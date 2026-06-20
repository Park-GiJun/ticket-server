package com.gijun.ticketserver.gateway

import com.gijun.ticketserver.infrastructure.config.security.JwtClaims
import com.gijun.ticketserver.infrastructure.config.security.JwtTokenValidator
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletRequestWrapper
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.util.AntPathMatcher
import org.springframework.web.filter.OncePerRequestFilter
import java.util.Collections
import java.util.Enumeration

/**
 * 게이트웨이 단일 인증 지점. Authorization 의 JWT 를 검증해 신원을 백엔드로 `X-User-*` 헤더로 전달한다.
 * 클라이언트가 보낸 `X-User-*` 헤더는 신뢰하지 않으므로 항상 게이트웨이가 덮어쓰거나 제거한다.
 */
class JwtAuthGatewayFilter(
    private val validator: JwtTokenValidator,
) : OncePerRequestFilter() {

    private val pathMatcher = AntPathMatcher()

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        // 공개 경로: 인증 없이 통과하되 위조 신원 헤더는 제거한다.
        if (isPublic(request)) {
            filterChain.doFilter(IdentityHeaderRequest(request, emptyMap()), response)
            return
        }

        val claims = resolveToken(request)?.let { validator.validate(it) }
        if (claims == null) {
            writeUnauthorized(response)
            return
        }

        filterChain.doFilter(IdentityHeaderRequest(request, identityHeaders(claims)), response)
    }

    private fun identityHeaders(claims: JwtClaims): Map<String, String> = mapOf(
        HEADER_USER_ID to claims.userId.toString(),
        HEADER_USER_EMAIL to claims.email,
        HEADER_USER_ROLE to claims.role,
    )

    private fun resolveToken(request: HttpServletRequest): String? {
        val header = request.getHeader(HttpHeaders.AUTHORIZATION) ?: return null
        return if (header.startsWith(BEARER_PREFIX)) header.substring(BEARER_PREFIX.length) else null
    }

    private fun isPublic(request: HttpServletRequest): Boolean {
        if (PUBLIC_PATHS.any { pathMatcher.match(it, request.requestURI) }) return true
        // 공연 조회(GET)는 비로그인도 허용한다(생성/수정은 인증 필요).
        return request.method.equals("GET", ignoreCase = true) &&
            PUBLIC_GET_PATHS.any { pathMatcher.match(it, request.requestURI) }
    }

    private fun writeUnauthorized(response: HttpServletResponse) {
        response.status = HttpServletResponse.SC_UNAUTHORIZED
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.characterEncoding = Charsets.UTF_8.name()
        response.writer.write("""{"status":401,"message":"인증이 필요합니다"}""")
    }

    companion object {
        private const val BEARER_PREFIX = "Bearer "
        const val HEADER_USER_ID = "X-User-Id"
        const val HEADER_USER_EMAIL = "X-User-Email"
        const val HEADER_USER_ROLE = "X-User-Role"
        private val PUBLIC_PATHS = listOf("/api/auth/**", "/actuator/**")

        // 비로그인도 허용하는 GET 전용 경로(공연 목록·상세·구역·좌석 조회).
        private val PUBLIC_GET_PATHS = listOf("/api/ticket-events/**")
    }
}

/**
 * `X-User-*` 신원 헤더를 게이트웨이가 단독 관리하도록 감싼다.
 * 들어온 요청의 동일 헤더는 무시하고, 인증된 경우에만 게이트웨이가 세팅한 값을 노출한다.
 */
private class IdentityHeaderRequest(
    request: HttpServletRequest,
    private val identity: Map<String, String>,
) : HttpServletRequestWrapper(request) {

    private val managed = setOf(
        JwtAuthGatewayFilter.HEADER_USER_ID.lowercase(),
        JwtAuthGatewayFilter.HEADER_USER_EMAIL.lowercase(),
        JwtAuthGatewayFilter.HEADER_USER_ROLE.lowercase(),
    )

    override fun getHeader(name: String): String? {
        if (name.lowercase() in managed) {
            return identity.entries.firstOrNull { it.key.equals(name, ignoreCase = true) }?.value
        }
        return super.getHeader(name)
    }

    override fun getHeaders(name: String): Enumeration<String> {
        if (name.lowercase() in managed) {
            val value = getHeader(name)
            return Collections.enumeration(value?.let { listOf(it) } ?: emptyList())
        }
        return super.getHeaders(name)
    }

    override fun getHeaderNames(): Enumeration<String> {
        val names = super.getHeaderNames().toList()
            .filter { it.lowercase() !in managed }
            .toMutableList()
        names.addAll(identity.keys)
        return Collections.enumeration(names)
    }
}

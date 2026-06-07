# 07. 보안 & JWT

## Spring Security 설정

`infrastructure/config/security/SecurityConfig.kt`

```kotlin
@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
) {
    @Bean fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .httpBasic { it.disable() }
            .formLogin { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests {
                it.requestMatchers(*PUBLIC_PATHS).permitAll()
                it.anyRequest().authenticated()
            }
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
        return http.build()
    }
}
```

### 핵심 결정

| 항목 | 설정 | 이유 |
|------|------|------|
| 세션 | `STATELESS` | JWT 기반, 서버 세션 미사용 |
| CSRF | disable | 비-쿠키 토큰 인증이라 불필요 |
| httpBasic/formLogin | disable | REST API 전용 |
| 비밀번호 | `BCryptPasswordEncoder` | 단방향 해싱(salt 내장) |

### 공개 경로 (`PUBLIC_PATHS`)

```
/api/auth/**          # 회원가입/로그인/비밀번호 재설정
/actuator/health/**   # 헬스체크
/swagger-ui/**, /swagger-ui.html, /v3/api-docs/**   # API 문서
```

그 외 모든 요청은 인증 필요(`anyRequest().authenticated()`).

## JWT 발급/검증 — JwtTokenProvider

`infrastructure/adapter/out/user/token/JwtTokenProvider.kt` (`UserTokenPort` 구현, jjwt 0.12.x)

```kotlin
private val key = Keys.hmacShaKeyFor(properties.secret.toByteArray(StandardCharsets.UTF_8))

override fun issueAccessToken(user: UserModel): IssuedToken {
    val token = Jwts.builder()
        .issuer(properties.issuer)
        .subject(user.id.toString())     // sub = userId
        .claim("email", user.email)
        .claim("role", user.role.name)
        .issuedAt(Date(now))
        .expiration(Date(now + properties.accessTokenExpirationMillis))
        .signWith(key)
        .compact()
    return IssuedToken(token, "Bearer", properties.accessTokenExpirationMillis / 1000)
}
```

- **클레임**: `sub`=userId, `email`, `role`, `iss`, `iat`, `exp`.
- 검증(`validateToken`)은 서명 + **issuer 일치**를 확인하고, 실패(`JwtException`/`IllegalArgumentException`)
  시 예외를 던지지 않고 `null` 반환 → 필터가 인증을 건너뛰게 함.

### JwtProperties

`infrastructure/security/JwtProperties.kt` — `@ConfigurationProperties(prefix = "jwt")`
(`@ConfigurationPropertiesScan` 이 `TicketServerApplication` 에 선언됨)

```yaml
jwt:
  secret: ...(32바이트 이상)
  access-token-expiration-millis: 3600000   # 1시간
  issuer: ticket-server
```

> ⚠️ HS256/512 서명 키는 **최소 32바이트(256bit)**. dev 기본값은 운영에서 반드시 교체.

## 인증 필터 — JwtAuthenticationFilter

`infrastructure/security/JwtAuthenticationFilter.kt` (`OncePerRequestFilter`)

```kotlin
resolveToken(request)                          // "Authorization: Bearer <token>"
    ?.let { userTokenPort.validateToken(it) }  // 유효하지 않으면 null
    ?.takeIf { SecurityContextHolder.getContext().authentication == null }
    ?.let { payload ->
        val principal = AuthenticatedUser(payload.userId, payload.email, payload.role)
        val authorities = listOf(SimpleGrantedAuthority("ROLE_${payload.role.name}"))
        val auth = UsernamePasswordAuthenticationToken(principal, null, authorities)
        auth.details = WebAuthenticationDetailsSource().buildDetails(request)
        SecurityContextHolder.getContext().authentication = auth
    }
filterChain.doFilter(request, response)
```

- `Bearer` 토큰을 파싱→검증→`SecurityContext` 에 인증 주입.
- principal 은 `AuthenticatedUser(userId, email, role)` → 컨트롤러에서 `@AuthenticationPrincipal` 로 사용.
- 권한은 `ROLE_USER`/`ROLE_ADMIN` 형태.

## 인증 컴포넌트 관계

```
요청 ──> JwtAuthenticationFilter ──(검증)──> UserTokenPort(JwtTokenProvider)
              │                                     │
              ▼                                     ▼
        SecurityContext              JwtProperties(secret/exp/issuer)
        (AuthenticatedUser)
```

package com.gijun.ticketserver.gateway

import com.gijun.ticketserver.infrastructure.config.security.JwtProperties
import com.gijun.ticketserver.infrastructure.config.security.JwtTokenValidator
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered

/**
 * 게이트웨이의 JWT 인증 구성. JwtProperties 는 common 패키지에 있어 게이트웨이 컴포넌트 스캔 범위 밖이므로
 * [EnableConfigurationProperties] 로 명시 등록한다. 발급 측(user-service)과 동일한 jwt.secret/issuer 를 사용한다.
 */
@Configuration
@EnableConfigurationProperties(JwtProperties::class)
class GatewaySecurityConfig {

    @Bean
    fun jwtTokenValidator(properties: JwtProperties): JwtTokenValidator = JwtTokenValidator(properties)

    @Bean
    fun jwtAuthFilterRegistration(
        validator: JwtTokenValidator,
    ): FilterRegistrationBean<JwtAuthGatewayFilter> {
        val registration = FilterRegistrationBean(JwtAuthGatewayFilter(validator))
        // 라우팅(forward)보다 먼저 인증이 끝나도록 최우선 순서로 둔다.
        registration.order = Ordered.HIGHEST_PRECEDENCE
        registration.addUrlPatterns("/*")
        return registration
    }
}

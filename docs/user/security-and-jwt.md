# 07. 보안 & JWT

## 0. MSA 인증 모델 (게이트웨이 단일 인증)

JWT **발급은 `user-service`**, **검증은 `gateway`** 가 맡는다. 양쪽은 `common` 모듈의
`JwtTokenValidator`/`JwtProperties` 를 통해 **동일한 `jwt.secret`·`jwt.issuer`** 를 공유한다.
바꿀 때는 gateway 와 user-service 의 `application.yml` 을 **함께** 수정해야 한다.

```
Client ──Bearer JWT──> gateway(JwtAuthGatewayFilter)
   ├─ 공개 경로(/api/auth/**, /actuator/**) → 그대로 통과(위조 X-User-* 헤더는 제거)
   ├─ 검증 실패/누락 → 401 {"status":401,"message":"인증이 필요합니다"}
   └─ 검증 성공 → X-User-Id / X-User-Email / X-User-Role 헤더를 붙여 백엔드로 라우팅
```

- 게이트웨이 필터(`gateway/JwtAuthGatewayFilter.kt`)는 `OncePerRequestFilter` 로 최우선 순서(`HIGHEST_PRECEDENCE`)에
  등록(`GatewaySecurityConfig`)된다. 클라이언트가 보낸 `X-User-*` 는 **신뢰하지 않고** 항상 덮어쓰거나 제거한다(`IdentityHeaderRequest`).
- ⚠️ **현재 다운스트림 서비스는 이 `X-User-*` 헤더를 아직 소비하지 않는다.**
  - `user-service` 는 자체 `SecurityConfig` + `JwtAuthenticationFilter` 로 **`Authorization` 토큰을 직접 재검증**하고
    `@AuthenticationPrincipal AuthenticatedUser` 로 신원을 얻는다(아래 본문). 게이트웨이를 우회한 직접 호출도 막힌다.
  - `ticket-event-service` 는 security 의존성이 없다(게이트웨이가 인증을 보장한다고 전제). 신원 헤더는 추후 사용 예정.

아래 본문은 **`user-service` 내부의 발급·검증** 구현이다.

## Spring Security 설정 (user-service)

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

`common` 모듈의 `infrastructure/config/security/JwtProperties.kt` — `@ConfigurationProperties(prefix = "jwt")`.
common 은 컴포넌트 스캔 범위 밖이라 모듈별로 명시 등록한다:
- `user-service`: `UserServiceApplication` 의 `@ConfigurationPropertiesScan`.
- `gateway`: `GatewaySecurityConfig` 의 `@EnableConfigurationProperties(JwtProperties::class)`.

```yaml
jwt:
  secret: ...(32바이트 이상)
  access-token-expiration-millis: 3600000   # 1시간
  issuer: ticket-server
```

> ⚠️ HS256/512 서명 키는 **최소 32바이트(256bit)**. dev 기본값은 운영에서 반드시 교체.

## 인증 필터 — JwtAuthenticationFilter (user-service)

`infrastructure/config/security/JwtAuthenticationFilter.kt` (`OncePerRequestFilter`)

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

# 05. 애플리케이션 레이어

유스케이스를 조립하는 계층. 도메인과 아웃바운드 포트를 오케스트레이션한다.

## 포트 (Ports)

### 인바운드 (in-port) — 유스케이스 인터페이스

`application/user/port/in/`

유스케이스는 **1 인터페이스 = 1 함수** 규칙을 따른다. 명령/조회 인터페이스를
각각 `UserCommandUseCases.kt` / `UserQueryUseCases.kt` 파일에 모아 둔다.

```kotlin
// UserCommandUseCases.kt — 명령(Command) 유스케이스
interface RegisterUserUseCase { fun register(command: RegisterUserCommand): UserResult }
interface LoginUseCase { fun login(command: LoginCommand): TokenResult }
interface RequestPasswordResetUseCase { fun requestPasswordReset(command: RequestPasswordResetCommand) }
interface ResetPasswordUseCase { fun resetPassword(command: ResetPasswordCommand) }

// UserQueryUseCases.kt — 조회(Query) 유스케이스
interface GetUserUseCase { fun getById(query: GetUserQuery): UserResult }
```

> **1 인터페이스 = 1 함수**: 유스케이스 단위로 인터페이스를 잘게 나눠 의존을 명시적으로
> 표현한다. 한 핸들러가 같은 그룹의 여러 유스케이스를 구현하고(예: `UserCommandHandler`
> 가 명령 유스케이스 4개 구현), 웹 어댑터는 자신이 실제로 쓰는 유스케이스만 주입받는다.
>
> 패키지명 `in` 은 Kotlin 예약어라 백틱(`` `in` ``)으로 선언/임포트한다.

### 아웃바운드 (out-port)

`application/user/port/out/`

| 포트 | 책임 | 구현 어댑터 |
|------|------|------------|
| `UserPersistencePort` | 사용자 영속화/조회 | JPA |
| `UserMemoryPort` | 재설정 토큰 저장(TTL) | Redis |
| `UserMessagePort` | 도메인 이벤트 발행 | Kafka |
| `UserTokenPort` | 인증 토큰 발급/검증 | JWT |

```kotlin
interface UserPersistencePort {
    fun save(user: UserModel): UserModel
    fun findById(id: Long): UserModel?
    fun findByEmail(email: String): UserModel?
    fun existsByEmail(email: String): Boolean
}

interface UserMemoryPort {
    fun savePasswordResetToken(token: String, email: String, ttl: Duration)
    fun findEmailByPasswordResetToken(token: String): String?
    fun deletePasswordResetToken(token: String)
}

interface UserMessagePort {
    fun sendUserRegistered(userId: Long, email: String)
    fun sendPasswordResetRequested(email: String, resetToken: String)
}

interface UserTokenPort {
    fun issueAccessToken(user: UserModel): IssuedToken
    fun validateToken(token: String): TokenPayload?
}
```

> **`UserTokenPort` 를 둔 이유**: 토큰 발급을 포트로 추상화해, 애플리케이션 레이어가
> 특정 JWT 라이브러리(jjwt)에 직접 의존하지 않게 한다. 구현은 인프라의 `JwtTokenProvider`.

## DTO

`application/user/dto/` — 계층 간 데이터 전달용. 웹 DTO 와 분리한다.

| 파일 | 타입 |
|------|------|
| `UserCommands.kt` | `RegisterUserCommand`, `LoginCommand`, `RequestPasswordResetCommand`, `ResetPasswordCommand` |
| `UserQueries.kt` | `GetUserQuery` |
| `UserResults.kt` | `UserResult`(+`from(UserModel)`), `TokenResult` |

## 핸들러 (유스케이스 구현)

### UserCommandHandler

`@Service @Transactional`, 명령 유스케이스 4개(`RegisterUserUseCase`, `LoginUseCase`,
`RequestPasswordResetUseCase`, `ResetPasswordUseCase`)를 함께 구현.

의존: `UserPersistencePort`, `UserMemoryPort`, `UserMessagePort`, `UserTokenPort`,
`PasswordEncoder`(Spring Security), `UserDomainService`(기본값 주입).

| 유스케이스 | 흐름 요약 |
|-----------|----------|
| `register` | 이메일 정규화 → 중복 검사 → 비밀번호 정책 검증 → BCrypt 인코딩 → 도메인 생성 → 저장 → `user.registered` 발행 |
| `login` | 이메일로 조회 → 비밀번호 매칭 → 활성 상태 확인 → JWT 발급 |
| `requestPasswordReset` | 조회(없어도 정상 반환) → 토큰 생성 → Redis 저장(TTL 30분) → `user.password-reset.requested` 발행 |
| `resetPassword` | 토큰으로 이메일 조회 → 사용자 조회 → 정책 검증 → 인코딩 → 저장 → 토큰 삭제 |

```kotlin
override fun login(command: LoginCommand): TokenResult {
    val email = userDomainService.normalizeEmail(command.email)
    val user = userPersistencePort.findByEmail(email)
        ?: throw UserException.InvalidCredentials()
    if (!passwordEncoder.matches(command.rawPassword, user.encodedPassword))
        throw UserException.InvalidCredentials()
    if (!user.isActive) throw UserException.InactiveUser()
    val issued = userTokenPort.issueAccessToken(user)
    return TokenResult(issued.token, issued.tokenType, issued.expiresInSeconds)
}
```

> **보안 메모**: `login` 은 "사용자 없음"과 "비밀번호 불일치"를 모두 동일한
> `InvalidCredentials`(401) 로 처리해 계정 존재 여부를 노출하지 않는다.
> `requestPasswordReset` 도 미존재 계정에 대해 동일하게 정상 응답(202)한다.

> **구현 메모**: Spring Security 7 의 `PasswordEncoder.encode()` 반환 타입이 nullable(`String?`)로
> 표기되어, `encodePassword()` 헬퍼에서 `requireNotNull` 으로 감싸 non-null 을 보장한다.

### UserQueryHandler

`@Service @Transactional(readOnly = true)`, `GetUserUseCase` 구현.
`getById` 로 사용자를 조회하고 없으면 `UserNotFound`.

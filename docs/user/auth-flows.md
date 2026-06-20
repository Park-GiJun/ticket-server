# 08. 인증 플로우

## 1. 회원가입 (Register)

```
Client ──POST /api/auth/register──> UserWebAdapter
   └─ RegisterRequest(email, password, name) 검증(@Valid)
        └─> UserCommandHandler.register
              ├─ normalizeEmail
              ├─ existsByEmail? ──예──> 409 EmailAlreadyExists
              ├─ validatePasswordPolicy
              ├─ BCrypt 인코딩
              ├─ UserPersistencePort.save
              └─ UserMessagePort.sendUserRegistered (Kafka)
        <── 201 Created + UserResponse
```

## 2. 로그인 (Login)

```
Client ──POST /api/auth/login──> UserWebAdapter
   └─ LoginRequest(email, password)
        └─> UserCommandHandler.login
              ├─ findByEmail ──없음──> 401 InvalidCredentials
              ├─ passwordEncoder.matches ──불일치──> 401 InvalidCredentials
              ├─ isActive? ──아니오──> 403 InactiveUser
              └─ UserTokenPort.issueAccessToken (JWT)
        <── 200 OK + TokenResponse(accessToken, "Bearer", expiresIn)
```

이후 보호된 API 호출 (MSA: 게이트웨이 경유):

```
Client ──GET /api/users/me  (Authorization: Bearer <token>)
   └─> gateway(JwtAuthGatewayFilter): 검증 실패 → 401 / 성공 → X-User-* 부착 후 라우팅
        └─> user-service: JwtAuthenticationFilter 가 Bearer 토큰을 재검증
              └─ SecurityContext(AuthenticatedUser)
                   └─> UserWebAdapter.me ──> UserQueryHandler.getById ──> 200 UserResponse
```

> 게이트웨이와 user-service 가 **이중으로** 검증한다(게이트웨이 우회 직접 호출 방어).
> `/api/auth/**` 발급 계열은 게이트웨이·user-service 양쪽에서 공개 경로다. → [07](./07-security-and-jwt.md)

## 3. 비밀번호 재설정 (Reset Password)

2단계로 분리: **요청(토큰 발급)** → **확정(비밀번호 변경)**.

```
[1단계] Client ──POST /api/auth/password-reset/request──> UserCommandHandler.requestPasswordReset
   ├─ findByEmail ──없음──> (그래도) 202 Accepted   ※ 계정 존재 여부 비노출
   ├─ token = UUID(no-dash)
   ├─ UserMemoryPort.savePasswordResetToken(token, email, TTL 30분)   (Redis)
   └─ UserMessagePort.sendPasswordResetRequested(email, token)        (Kafka → 메일 서비스)
   <── 202 Accepted

       (사용자는 메일로 받은 token 을 사용)

[2단계] Client ──POST /api/auth/password-reset/confirm──> UserCommandHandler.resetPassword
   ├─ findEmailByPasswordResetToken ──없음/만료──> 400 InvalidResetToken
   ├─ findByEmail ──없음──> 404 UserNotFound
   ├─ validatePasswordPolicy
   ├─ BCrypt 인코딩 → save (기존 사용자 갱신)
   └─ deletePasswordResetToken (1회용)
   <── 204 No Content
```

### 보안 포인트

| 위협 | 대응 |
|------|------|
| 계정 열거(enumeration) | 로그인 실패 메시지 단일화(401), 재설정 요청은 미존재여도 202 |
| 토큰 탈취/재사용 | Redis TTL 30분 + 사용 즉시 삭제(1회용) |
| 토큰 노출 | 응답 본문에 토큰 미포함, 메일 발송 경로(Kafka 소비자)로만 전달 |
| 비밀번호 평문 저장 | BCrypt 단방향 해싱 |
| 무차별 대입 | (예정) 로그인 시도 제한 / 계정 잠금(`UserStatus.LOCKED`) |

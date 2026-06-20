# 09. API 레퍼런스

베이스 경로: `/api`

## Swagger / OpenAPI

springdoc-openapi 로 자동 생성된다.

| 항목 | URL |
|------|-----|
| Swagger UI | `http://localhost:8080/swagger-ui.html` |
| OpenAPI JSON | `http://localhost:8080/v3/api-docs` |

- `infrastructure/config/OpenApiConfig.kt` 에서 **JWT Bearer 보안 스킴**(`bearerAuth`)을 등록한다.
- Swagger UI 의 **Authorize** 버튼에 로그인으로 받은 토큰을 넣으면 보호된 API(`/users/me`)를 테스트할 수 있다.
- 컨트롤러(`UserWebAdapter`)에 `@Tag`/`@Operation`/`@ApiResponses` 로 설명이 부여돼 있다.
- 공개 경로라 인증 없이 접근 가능하도록 `/swagger-ui/**`, `/v3/api-docs/**` 를 `permitAll` 처리 → [07](./07-security-and-jwt.md).

## 엔드포인트 요약

| Method | Path | 설명 | 인증 | 성공 코드 |
|--------|------|------|------|----------|
| POST | `/api/auth/register` | 회원가입 | 공개 | 201 |
| POST | `/api/auth/login` | 로그인(토큰 발급) | 공개 | 200 |
| POST | `/api/auth/password-reset/request` | 재설정 요청 | 공개 | 202 |
| POST | `/api/auth/password-reset/confirm` | 재설정 확정 | 공개 | 204 |
| GET | `/api/users/me` | 내 정보 조회 | **Bearer** | 200 |

---

## 회원가입

`POST /api/auth/register`

```json
// Request
{ "email": "user@example.com", "password": "password123", "name": "홍길동" }
```
```json
// 201 Created
{ "id": 1, "email": "user@example.com", "name": "홍길동",
  "role": "USER", "status": "ACTIVE", "createdAt": "2026-06-07T12:00:00Z" }
```

검증: `email`(형식), `password`(8~64자), `name`(최대 50자).

## 로그인

`POST /api/auth/login`

```json
// Request
{ "email": "user@example.com", "password": "password123" }
```
```json
// 200 OK
{ "accessToken": "eyJhbGciOi...", "tokenType": "Bearer", "expiresIn": 3600 }
```

이후 요청 헤더: `Authorization: Bearer <accessToken>`

## 비밀번호 재설정 — 요청

`POST /api/auth/password-reset/request`

```json
// Request
{ "email": "user@example.com" }
```
- **202 Accepted** (본문 없음). 계정이 없어도 동일하게 202(계정 열거 방지).
- 토큰은 응답에 포함되지 않고 메일 발송 채널(Kafka 소비자)로 전달된다.

## 비밀번호 재설정 — 확정

`POST /api/auth/password-reset/confirm`

```json
// Request
{ "token": "9f2c1a...", "newPassword": "newPassword456" }
```
- **204 No Content**.

## 내 정보 조회

`GET /api/users/me`  (헤더: `Authorization: Bearer <token>`)

```json
// 200 OK
{ "id": 1, "email": "user@example.com", "name": "홍길동",
  "role": "USER", "status": "ACTIVE", "createdAt": "2026-06-07T12:00:00Z" }
```

---

## 에러 응답 포맷

`infrastructure/config/GlobalExceptionHandler.kt` 가 일관된 형식으로 반환한다.

```json
{ "status": 409, "message": "이미 사용 중인 이메일입니다: user@example.com", "fieldErrors": {} }
```

검증 실패(`MethodArgumentNotValidException`) 시:

```json
{ "status": 400, "message": "요청 값이 올바르지 않습니다",
  "fieldErrors": { "email": "올바른 형식의 이메일 주소여야 합니다", "password": "크기가 8에서 64 사이여야 합니다" } }
```

### 도메인 예외 → HTTP 매핑

| 예외 | HTTP |
|------|------|
| `EmailAlreadyExists` | 409 Conflict |
| `UserNotFound` | 404 Not Found |
| `InvalidCredentials` | 401 Unauthorized |
| `InactiveUser` | 403 Forbidden |
| `InvalidEmail` / `InvalidPassword` / `InvalidResetToken` | 400 Bad Request |
| Bean Validation 실패 | 400 Bad Request (+ `fieldErrors`) |

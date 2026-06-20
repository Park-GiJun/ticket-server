# 04. 도메인 레이어

프레임워크에 의존하지 않는 **순수 Kotlin** 코드. 비즈니스 핵심 규칙을 담는다.

## UserModel

`domain/model/UserModel.kt`

> `UserRole`/`UserStatus` enum 은 `domain/enums/` 패키지의 독립 파일로 분리돼 있다
> (`domain/enums/UserRole.kt`, `domain/enums/UserStatus.kt`). 도메인 전반에서 공유하는
> 값 타입을 모델 파일과 분리해 재사용·가독성을 높였다.

```kotlin
// domain/enums/UserRole.kt
enum class UserRole { USER, ADMIN }
// domain/enums/UserStatus.kt
enum class UserStatus { ACTIVE, INACTIVE, LOCKED }

// domain/model/UserModel.kt
data class UserModel(
    val id: Long? = null,
    val email: String,
    val encodedPassword: String,   // 항상 인코딩된 값만 보관(평문 금지)
    val name: String,
    val role: UserRole = UserRole.USER,
    val status: UserStatus = UserStatus.ACTIVE,
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null,
) {
    val isActive: Boolean get() = status == UserStatus.ACTIVE
    fun withPassword(newEncodedPassword: String): UserModel = copy(encodedPassword = newEncodedPassword)
}
```

- **불변(immutable) data class** — 상태 변경은 `copy` 기반(`withPassword`)으로.
- `encodedPassword` 네이밍으로 "평문이 아니다"라는 불변식을 표현.
- `id == null` 은 아직 영속화되지 않은 신규 모델을 의미.

## UserDomainService

`domain/service/UserDomainService.kt`

상태 없는 순수 클래스. **이메일/비밀번호 정책**과 생성 불변식을 담당한다.
Spring 빈으로 등록하지 않고 핸들러에서 직접 생성해 쓴다(도메인의 프레임워크 독립성 유지).

| 메서드 | 역할 |
|--------|------|
| `createUser(email, encodedPassword, name, role)` | 이메일 정규화·형식 검증 후 `UserModel` 생성 |
| `normalizeEmail(email)` | `trim().lowercase()` |
| `validateEmailFormat(email)` | 정규식 검증, 실패 시 `InvalidEmail` |
| `validatePasswordPolicy(rawPassword)` | 길이 ≥ 8, 숫자·영문자 포함 검증 |

```kotlin
fun validatePasswordPolicy(rawPassword: String) {
    if (rawPassword.length < 8) throw UserException.InvalidPassword("비밀번호는 8자 이상이어야 합니다")
    if (rawPassword.none { it.isDigit() }) throw UserException.InvalidPassword("비밀번호에 숫자를 포함해야 합니다")
    if (rawPassword.none { it.isLetter() }) throw UserException.InvalidPassword("비밀번호에 영문자를 포함해야 합니다")
}
```

> 비밀번호 정책은 **평문 검증**이므로 인코딩 이전에 호출한다. 인코딩(BCrypt)은 인프라 관심사라
> 도메인이 아닌 애플리케이션 핸들러에서 `PasswordEncoder` 로 수행한다.

## UserException

`domain/exception/UserException.kt`

`sealed class` 로 정의해 예외 처리기에서 `when` 으로 **누락 없이** 매핑한다.

| 예외 | 의미 | HTTP (→ [API 레퍼런스](./api-reference.md)) |
|------|------|------|
| `EmailAlreadyExists` | 이메일 중복 | 409 |
| `UserNotFound` | 사용자 없음 | 404 |
| `InvalidCredentials` | 인증 실패 | 401 |
| `InactiveUser` | 비활성 계정 | 403 |
| `InvalidEmail` | 이메일 형식 오류 | 400 |
| `InvalidPassword` | 비밀번호 정책 위반 | 400 |
| `InvalidResetToken` | 재설정 토큰 무효/만료 | 400 |

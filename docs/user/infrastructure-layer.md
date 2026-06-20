# 06. 인프라스트럭처 레이어

아웃바운드 포트의 실제 구현체(어댑터)들. 기술 의존성이 여기에 격리된다.

## 영속성 (JPA)

`infrastructure/adapter/out/user/persistence/`

### UserEntity (`entity/`)

```kotlin
@Entity
@Table(name = "users")
class UserEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long? = null,
    @Column(nullable = false, unique = true) var email: String,
    @Column(nullable = false) var password: String,
    @Column(nullable = false) var name: String,
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) var role: UserRole,
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) var status: UserStatus,
    @CreationTimestamp @Column(nullable = false, updatable = false) var createdAt: Instant? = null,
    @UpdateTimestamp @Column(nullable = false) var updatedAt: Instant? = null,
) {
    fun toModel(): UserModel { ... }
    companion object { fun fromModel(model: UserModel): UserEntity { ... } }
}
```

- 도메인 `UserModel` ↔ `UserEntity` 변환을 엔티티에 캡슐화(`toModel`/`fromModel`).
- enum 은 `EnumType.STRING` 으로 저장(순서 변경에 안전).
- 생성/수정 시각은 Hibernate `@CreationTimestamp`/`@UpdateTimestamp` 로 자동 관리.
- `kotlin("plugin.jpa")` + `allOpen`(Entity/MappedSuperclass/Embeddable) 설정으로
  JPA 프록시/no-arg 생성자 요건 충족 → [02 문서](./02-build-and-gradle.md).

### UserPersistenceRepository (`repository/`)

```kotlin
interface UserPersistenceRepository : JpaRepository<UserEntity, Long> {
    fun findByEmail(email: String): UserEntity?
    fun existsByEmail(email: String): Boolean
}
```

### UserPersistenceAdapter (`adapter/`)

`@Component`, `UserPersistencePort` 구현. 엔티티↔도메인 변환을 담당.

```kotlin
override fun save(user: UserModel): UserModel {
    val entity = user.id
        ?.let { id -> repository.findById(id).orElse(null)?.applyFrom(user) }
        ?: UserEntity.fromModel(user)
    return repository.save(entity).toModel()
}
```

> `save` 는 **id 가 있으면 기존 엔티티를 로드해 변경분을 반영(dirty checking)**, 없으면 신규 생성.
> 비밀번호 재설정처럼 기존 사용자를 갱신하는 경우 createdAt 등이 보존된다.

## Redis (Memory)

`infrastructure/adapter/out/user/memory/UserMemoryAdapter.kt`

`StringRedisTemplate` 기반. 비밀번호 재설정 토큰을 **TTL** 과 함께 저장한다.

```kotlin
override fun savePasswordResetToken(token: String, email: String, ttl: Duration) {
    redisTemplate.opsForValue().set(key(token), email, ttl)   // key = "user:password-reset:{token}"
}
```

| 메서드 | 동작 |
|--------|------|
| `savePasswordResetToken` | `user:password-reset:{token}` → email, TTL(기본 30분) |
| `findEmailByPasswordResetToken` | 토큰으로 email 조회(만료 시 null) |
| `deletePasswordResetToken` | 사용 후 토큰 삭제(1회용 보장) |

## Kafka (Message)

`infrastructure/adapter/out/user/message/UserMessageAdapter.kt`

`KafkaTemplate<String, String>` 기반. 도메인 이벤트를 발행한다.

| 메서드 | 토픽 | key / value |
|--------|------|-------------|
| `sendUserRegistered` | `user.registered` | userId / email |
| `sendPasswordResetRequested` | `user.password-reset.requested` | email / resetToken |

```kotlin
private fun send(topic: String, key: String, payload: String) {
    // 브로커 장애가 인증 흐름을 막지 않도록 best-effort(실패는 로깅만)
    kafkaTemplate.send(topic, key, payload).whenComplete { _, ex ->
        if (ex != null) log.warn("Kafka 발행 실패 topic={} key={}: {}", topic, key, ex.message)
    }
}
```

> **설계 의도**: 비밀번호 재설정 토큰은 실제로는 이 이벤트를 소비하는 **메일 발송 서비스**가
> 사용자에게 전달한다(서버 응답 본문에는 토큰을 노출하지 않음). 발행은 비동기/best-effort 라
> 브로커 장애 시에도 회원가입/재설정 요청 자체는 실패하지 않는다.

## JWT (Token)

`infrastructure/adapter/out/user/token/JwtTokenProvider.kt` — `UserTokenPort` 구현.
상세는 [07. 보안 & JWT](./07-security-and-jwt.md) 참고.

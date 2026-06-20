# 사용자(User) 도메인

`user-service` 가 담당하는 **사용자/인증 도메인**(회원가입·로그인·비밀번호 재설정·내 정보)의
설계·구현 문서. 헥사고날(Ports & Adapters) + CQRS 구조를 따른다.

> 상위: [전체 문서 인덱스](../README.md)

## 도메인 한눈에 보기

- **인증 모델**: JWT 기반 Stateless 인증, BCrypt 비밀번호 해싱.
- **발급/검증 분리**: 토큰 발급은 user-service, 게이트웨이 진입 검증은 gateway 가 담당
  (공통 `JwtTokenValidator` 공유). user-service 도 자체 `SecurityConfig` 로 직접 호출을 방어한다.
- **비밀번호 재설정**: Redis(토큰 저장) + Kafka(메일 발행) 기반.

## 레이어별 문서

| 레이어 | 문서 | 내용 |
|--------|------|------|
| 도메인 | [domain-layer](./domain-layer.md) | `UserModel`, `UserDomainService`, 도메인 예외 |
| 애플리케이션 | [application-layer](./application-layer.md) | 포트(in/out), DTO, 유스케이스 핸들러 |
| 인프라스트럭처 | [infrastructure-layer](./infrastructure-layer.md) | JPA / Redis / Kafka 어댑터 |
| 보안 | [security-and-jwt](./security-and-jwt.md) | Spring Security 설정, JWT 발급/검증, 인증 필터 |
| 인증 플로우 | [auth-flows](./auth-flows.md) | 회원가입 / 로그인 / 비밀번호 재설정 시퀀스 |
| API 레퍼런스 | [api-reference](./api-reference.md) | 엔드포인트, 요청/응답, 에러 코드, Swagger |

> User 도메인 문서는 레이어당 단일 파일(평면 구조)이다. 문서가 비대해지면 ticket-event 처럼
> 레이어별 디렉토리로 분리한다.

# ticket-server 배포 (CD)

홈서버 TeamCity(`범용 CI/CD`)의 **`TicketServer / Deploy`** 빌드 구성(수동 트리거)에서
`deploy/deploy.sh` 가 실행된다. 코어 4서비스만 배포한다.

| 서비스 | 이미지 | 포트 | 비고 |
|--------|--------|------|------|
| discovery-server | `ticketserver-discovery` | 18761(내부) | Eureka |
| gateway | `ticketserver-gateway` | **host 18080** | 단일 외부 진입점 |
| user-service | `ticketserver-user` | 18081(내부) | DB+Redis+Kafka |
| ticket-event-service | `ticketserver-events` | 18082(내부) | DB |

- 네트워크 `ticketserver-net`(서비스 간 통신·Eureka `lb://`). DB/Redis/Kafka 는 공용 `infra-*`(`210.121.177.150`).
- 앱 `application.yml` 이 **개발=배포 동일하게 실제 서버 인프라**를 직접 사용한다(개인 프로젝트 정책). 따라서
  배포에서 추가로 주는 건 Eureka 주소(`localhost`→`ticket-discovery`)와 포트뿐 — **비밀 파라미터 주입 불필요**.
- DB: `infra-postgres` 의 `ticketserver` DB/계정(생성 완료). 4서비스가 같은 DB에 도메인별 테이블 생성(ddl-auto=update).

## 수동 배포
TeamCity 에서 `Deploy` 실행, 또는 에이전트/서버에서:
```bash
deploy/deploy.sh           # bootJar 빌드 → 이미지 빌드 → compose up
```
외부 진입점: `http://210.121.177.150:18080`

# ticket-server 배포 (CD)

홈서버 TeamCity(`범용 CI/CD`)의 **`TicketServer / Deploy`** 빌드 구성(수동 트리거)에서
`deploy/deploy.sh` 가 실행된다. 코어 4서비스만 배포한다.

| 서비스 | 이미지 | 포트 | 비고 |
|--------|--------|------|------|
| discovery-server | `ticketserver-discovery` | 18761(내부) | Eureka |
| gateway | `ticketserver-gateway` | **host 18080** | 단일 외부 진입점 |
| user-service | `ticketserver-user` | 18081(내부) | DB+Redis+Kafka |
| ticket-event-service | `ticketserver-events` | 18082(내부) | DB |

- 네트워크 `ticketserver-net`(서비스 간 통신·Eureka `lb://`). DB/Redis/Kafka 는 공용 `infra-*` 인프라를 사용한다.
- DB/Redis/Kafka/JWT **자격증명은 `deploy/.env`(git 미추적) 에서 주입**한다(`deploy/.env.example` 참고).
  `deploy.sh` 가 `.env` 를 읽어 compose 변수 치환 + 컨테이너 env 로 전달하고, Eureka 주소(`localhost`→`ticket-discovery`)·포트는 compose 가 덮어쓴다.
- DB: `infra-postgres` 의 `ticketserver` DB/계정. 4서비스가 같은 DB에 도메인별 테이블 생성(ddl-auto=update).

## 수동 배포
사전 준비(최초 1회): `cp deploy/.env.example deploy/.env` 후 실제 값 입력.

TeamCity 에서 `Deploy` 실행, 또는 에이전트/서버에서:
```bash
deploy/deploy.sh           # bootJar 빌드 → 이미지 빌드 → compose up
```
외부 진입점: `http://<PUBLIC_HOST>:18080`

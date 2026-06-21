#!/usr/bin/env bash
# ticket-server 배포 스크립트 — TeamCity 'Deploy' 빌드 구성(수동 트리거)에서 실행.
# 에이전트(docker.sock 보유)에서 동작: bootJar 빌드 → 모듈별 이미지 빌드 → compose 로 배포.
#
# DB/Redis/Kafka/JWT 자격증명은 deploy/.env (git 미추적) 에서 주입한다. deploy/.env.example 참고.
# 선택 환경변수: GATEWAY_PORT(기본 18080), TAG(기본 latest), PUBLIC_HOST(안내 표시용)
set -euo pipefail

cd "$(dirname "$0")/.."          # 빌드 루트(ticket-server-be)로 이동
ROOT="$(pwd)"
DEPLOY="$ROOT/deploy"

# 비밀값/인프라 주소 주입 (.env 가 있으면 compose 변수 치환·컨테이너 env 로 전달)
ENV_FILE="$DEPLOY/.env"
ENV_ARG=""
if [ -f "$ENV_FILE" ]; then
  ENV_ARG="--env-file $ENV_FILE"
  set -a; . "$ENV_FILE"; set +a   # PUBLIC_HOST 등 스크립트에서도 참조
else
  echo "⚠️  $ENV_FILE 없음 — deploy/.env.example 을 복사해 채우세요(자격증명 미주입 시 기동 실패)."
fi

# 모듈명(이미지) => Gradle 모듈 디렉토리
MODULES="discovery:discovery-server gateway:gateway user:user-service events:ticket-event-service"

echo "== 1) bootJar 빌드 =="
# gradlew 가 체크아웃에서 실행권한을 잃을 수 있어 sh 로 호출(권한 무관)
sh ./gradlew --no-daemon \
  :discovery-server:bootJar :gateway:bootJar :user-service:bootJar :ticket-event-service:bootJar

echo "== 2) 모듈별 이미지 빌드 =="
for pair in $MODULES; do
  name="${pair%%:*}"; mod="${pair##*:}"
  jar="$(ls "$ROOT/$mod/build/libs/"*.jar | grep -v -- '-plain' | head -1)"
  echo " - ticketserver-$name <= ${jar#$ROOT/}"
  cp "$jar" "$DEPLOY/app.jar"
  docker build -t "ticketserver-$name:${TAG:-latest}" \
    --build-arg JAR_FILE=app.jar -f "$DEPLOY/Dockerfile" "$DEPLOY"
  rm -f "$DEPLOY/app.jar"
done

echo "== 2.5) FE 이미지 빌드 =="
docker build -t "ticketserver-fe:${TAG:-latest}" "$ROOT/../ticket-server-fe"

echo "== 3) 배포(compose up) =="
docker compose $ENV_ARG -f "$DEPLOY/docker-compose.yml" up -d --remove-orphans

echo "== 4) 상태 =="
docker compose $ENV_ARG -f "$DEPLOY/docker-compose.yml" ps
echo "게이트웨이(외부 진입점): http://${PUBLIC_HOST:-localhost}:${GATEWAY_PORT:-18080}"

#!/usr/bin/env bash
# ticket-server 배포 스크립트 — TeamCity 'Deploy' 빌드 구성(수동 트리거)에서 실행.
# 에이전트(docker.sock 보유)에서 동작: bootJar 빌드 → 모듈별 이미지 빌드 → compose 로 배포.
#
# 앱 application.yml 이 공용 인프라/비밀값을 직접 갖고 있어 별도 파라미터 주입은 불필요.
# 선택 환경변수: GATEWAY_PORT(기본 18080), TAG(기본 latest)
set -euo pipefail

cd "$(dirname "$0")/.."          # 빌드 루트(ticket-server-be)로 이동
ROOT="$(pwd)"
DEPLOY="$ROOT/deploy"

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
docker compose -f "$DEPLOY/docker-compose.yml" up -d --remove-orphans

echo "== 4) 상태 =="
docker compose -f "$DEPLOY/docker-compose.yml" ps
echo "게이트웨이(외부 진입점): http://210.121.177.150:${GATEWAY_PORT:-18080}"

# 공용 런타임 이미지 — TeamCity 가 빌드한 bootJar 를 받아 실행한다.
# deploy.sh 가 모듈별 jar 를 app.jar 로 스테이징한 뒤 --build-arg JAR_FILE 로 전달한다.
FROM eclipse-temurin:25-jdk-alpine

WORKDIR /app
ARG JAR_FILE
COPY ${JAR_FILE} app.jar

# JAVA_OPTS 로 메모리/프로파일 등 런타임 조정 가능
ENV JAVA_OPTS=""
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]

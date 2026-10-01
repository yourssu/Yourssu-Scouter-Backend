FROM eclipse-temurin:21-jdk-alpine as base
ENV TZ=Asia/Seoul
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
WORKDIR /app
COPY build/libs/*-SNAPSHOT.jar app.jar
RUN mkdir -p /app/logs
EXPOSE ${SERVER_PORT:-8080}

# 작은 인스턴스(RAM ~1.8GB)에서 여러 컨테이너가 동시에 도는 환경이라 swap thrashing 방지를 위해 JVM 메모리 상한을 명시
# - live set 약 90MB 기준 -Xmx256m, metaspace 실사용 약 160MB라 MaxMetaspaceSize는 256m(안전장치)
# - docker run -e 또는 --env-file 에서 JAVA_TOOL_OPTIONS를 지정하면 덮어쓸 수 있음
ENV JAVA_TOOL_OPTIONS="-Xms128m -Xmx256m -XX:MaxMetaspaceSize=256m -XX:ReservedCodeCacheSize=64m -Xss512k -XX:+UseSerialGC"

# 직접 실행 (스크립트 없이)
ENTRYPOINT ["java", "-Duser.timezone=Asia/Seoul", "-jar", "/app/app.jar"]
CMD ["--spring.profiles.active=${ENVIRONMENT:-dev}", "--server.port=${SERVER_PORT:-8080}"]

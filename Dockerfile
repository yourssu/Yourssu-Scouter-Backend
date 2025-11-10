FROM eclipse-temurin:21-jdk-alpine as base
ENV TZ=Asia/Seoul
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
WORKDIR /app
COPY build/libs/*-SNAPSHOT.jar app.jar
RUN mkdir -p /app/logs
EXPOSE ${SERVER_PORT:-8080}

# 직접 실행 (스크립트 없이)
ENTRYPOINT ["java", "-Duser.timezone=Asia/Seoul", "-jar", "/app/app.jar"]
CMD ["--spring.profiles.active=${ENVIRONMENT:-dev}", "--server.port=${SERVER_PORT:-8080}"]

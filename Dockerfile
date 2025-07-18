FROM openjdk:21

COPY build/libs/scouter-0.0.1-SNAPSHOT.jar /

ENTRYPOINT ["java", "-Duser.timezone=Asia/Seoul", "-Dspring.profiles.active=dev", "-jar", "/scouter-0.0.1-SNAPSHOT.jar"]

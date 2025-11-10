FROM openjdk:21-slim as base

#Set timezone to KST
ENV TZ=Asia/Seoul
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

WORKDIR /app

# Copy pre-built JAR (built in Github Action)
COPY build/libs/*-SNAPSHOT.jar app.jar

# Create logs directory
RUN mkdir -p /app/logs

# Create startup script
RUN printf '#!/bin/bash\n\
set -e \n\
\n\
# Start Spring boot application\n\
java -Duser.timezone=Asia/Seoul -jar /app/app.jar --spring.profiles.active=${ENVIRONMENT:-dev} --server-port={SERVER_PORT:-8080} &\n\
SPRING_PID=$!\n\
\n\
# Wait for process to exit\n\
wait $SPRING_PID\n\
' > /app/start.sh && chmod +x /app/start.sh

# Expose port (default 8080, overridden by SERVER_PORT
EXPOSE ${SERVER_PORT:-8080}

# use the startup script as entrypoint
ENTRYPOINT ["/bin/bash", "/app/start.sh"]

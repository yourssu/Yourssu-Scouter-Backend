#!/bin/bash

ENV_FILE="${ENV_FILE:-.env}"
CONTAINER_SUFFIX="${CONTAINER_SUFFIX:-}"

if [ ! -f "$ENV_FILE" ]; then
    echo "Environment file not found: $ENV_FILE"
    exit 1
fi

# Load environment variables
source "$ENV_FILE"

# 필수 변수 검증
if [ -z "$SERVER_PORT" ]; then
    echo "ERROR: SERVER_PORT is not set in $ENV_FILE"
    exit 1
fi

# Variables
if [ -n "$CONTAINER_SUFFIX" ]; then
    CONTAINER_NAME="${PROJECT_NAME}-${CONTAINER_SUFFIX}-container"
else
    CONTAINER_NAME="${PROJECT_NAME}-container"
fi
IMAGE_TAG="${IMAGE_TAG:-latest}"
IMAGE_NAME="$ECR_REGISTRY/yourssu/${PROJECT_NAME}:${IMAGE_TAG}"

echo "Starting deployment process..."
echo "Environment file: $ENV_FILE"
echo "Container name: $CONTAINER_NAME"
echo "Image name: $IMAGE_NAME"

# 롤백용: 기존 컨테이너의 이미지 ID 저장 (Health check 실패 시 복원)
PREV_IMAGE_ID=""
if [ "$(docker ps -aq -f name="$CONTAINER_NAME")" ]; then
    PREV_IMAGE_ID=$(docker inspect -f '{{.Image}}' "$CONTAINER_NAME" 2>/dev/null || true)
fi

# ECR Public 인증 (만료된 토큰으로 인한 pull 실패 방지)
echo "Authenticating with ECR Public..."
aws ecr-public get-login-password --region us-east-1 | docker login --username AWS --password-stdin public.ecr.aws 2>/dev/null || {
    echo "WARN: ECR login failed. Removing stale credentials and retrying without auth..."
    docker logout public.ecr.aws 2>/dev/null || true
}

# Pull the latest image
echo "Pulling the latest image..."
docker pull "$IMAGE_NAME"

# Check if container is running
if [ "$(docker ps -q -f name="$CONTAINER_NAME")" ]; then
    echo "Stopping existing container..."
    docker stop "$CONTAINER_NAME"
fi

# Remove existing container if it exists
if [ "$(docker ps -aq -f name="$CONTAINER_NAME")" ]; then
    echo "Removing existing container..."
    docker rm "$CONTAINER_NAME"
fi

# Run the new container
echo "Starting new container..."
docker run -d \
  --name "$CONTAINER_NAME" \
  --restart unless-stopped \
  -p "$SERVER_PORT":"$SERVER_PORT" \
  -v "$(pwd)/logs:/app/logs" \
  --env-file "$ENV_FILE" \
  "$IMAGE_NAME"

# Health check: 앱이 정상 기동했는지 확인 (Hibernate validate 실패 등 감지)
echo "Waiting for application to become healthy..."
sleep 10  # Spring Boot 기동 시간 초기 대기
MAX_ATTEMPTS=30
INTERVAL=2

for i in $(seq 1 $MAX_ATTEMPTS); do
  if curl -sf "http://localhost:${SERVER_PORT}/actuator/health" > /dev/null 2>&1; then
    echo "Application is healthy."
    echo "Deployment completed successfully!"
    echo "Container status:"
    docker ps -f name="$CONTAINER_NAME"
    # 성공 시에만 오래된 이미지 정리
    echo "Cleaning up old images..."
    docker images "$ECR_REGISTRY"/yourssu/"${PROJECT_NAME}" --format "table {{.Repository}}\t{{.Tag}}\t{{.ID}}\t{{.CreatedAt}}" | tail -n +2 | sort -k4 -r | tail -n +4 | awk '{print $3}' | xargs -r docker rmi 2>/dev/null || true
    exit 0
  fi
  echo "Attempt $i/$MAX_ATTEMPTS - waiting..."
  sleep $INTERVAL
done

echo "ERROR: Application failed to become healthy within $((MAX_ATTEMPTS * INTERVAL)) seconds."
echo "=== Container logs (last 100 lines) ==="
docker logs "$CONTAINER_NAME" --tail 100 2>&1 || true

# 실패한 컨테이너 정리
echo "Stopping failed container..."
docker stop "$CONTAINER_NAME" 2>/dev/null || true
docker rm "$CONTAINER_NAME" 2>/dev/null || true

# 롤백: 이전 이미지로 복원 (서버 중단 방지)
if [ -n "$PREV_IMAGE_ID" ]; then
    echo "Rolling back to previous image..."
    docker run -d \
      --name "$CONTAINER_NAME" \
      --restart unless-stopped \
      -p "$SERVER_PORT":"$SERVER_PORT" \
      -v "$(pwd)/logs:/app/logs" \
      --env-file "$ENV_FILE" \
      "$PREV_IMAGE_ID"
    echo "Rollback completed. Previous version is running."
else
    echo "No previous container to rollback. Server is down."
fi

exit 1

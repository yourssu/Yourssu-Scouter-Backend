#!/bin/bash

# Load environment variables
source .env

# Variables
CONTAINER_NAME="${PROJECT_NAME}-container"
IMAGE_TAG="${IMAGE_TAG:-latest}"
IMAGE_NAME="$ECR_REGISTRY/yourssu/${PROJECT_NAME}:${IMAGE_TAG}"

echo "Starting deployment process..."
echo "Container name: $CONTAINER_NAME"
echo "Image name: $IMAGE_NAME"

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

# Remove old images (keep only the 3 most recent)
echo "Cleaning up old images..."
docker images "$ECR_REGISTRY"/yourssu/"${PROJECT_NAME}" --format "table {{.Repository}}\t{{.Tag}}\t{{.ID}}\t{{.CreatedAt}}" | tail -n +2 | sort -k4 -r | tail -n +4 | awk '{print $3}' | xargs -r docker rmi

# Run the new container
echo "Starting new container..."
docker run -d \
  --name "$CONTAINER_NAME" \
  --restart unless-stopped \
  -p "$SERVER_PORT":"$SERVER_PORT" \
  -v $(pwd)/logs:/app/logs \
  --env-file .env \
  "$IMAGE_NAME"

echo "Deployment completed successfully!"
echo "Container status:"
docker ps -f name="$CONTAINER_NAME"

#!/bin/bash
./gradlew build

# Define variables for Docker Hub account and image details
DOCKERHUB_USERNAME=tmdfl36
IMAGE_NAME=inu_portal
TAG=latest  # or specify your desired tag/version

# Build the Docker image
docker buildx build --platform linux/arm64 -t $DOCKERHUB_USERNAME/$IMAGE_NAME:$TAG .
# Log in to Docker Hub (if not logged in)
# docker login -u $DOCKERHUB_USERNAME

# Push the Docker image to Docker Hub
docker push $DOCKERHUB_USERNAME/$IMAGE_NAME:$TAG


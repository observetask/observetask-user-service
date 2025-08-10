#!/bin/bash
set -e

SERVICE_NAME="observetask-user-service"
echo "🚀 Building and deploying $SERVICE_NAME..."

mvn clean package -DskipTests
eval $(minikube docker-env)
docker build -t observetask/$SERVICE_NAME:latest .
kubectl apply -f k8s/
echo "✅ Deployment complete!"

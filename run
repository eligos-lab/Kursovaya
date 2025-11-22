#!/bin/sh
# Используем /bin/sh вместо /bin/bash для максимальной совместимости

echo "Building application in Docker..."
docker-compose up --build

echo "Starting application..."
java -jar target/game-price-tracker-1.0-SNAPSHOT.jar

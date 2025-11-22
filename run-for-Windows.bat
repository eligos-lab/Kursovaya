#!/bin/sh
echo "🔨 Building Java application..."

# Если JAR нет или исходники изменились - собираем
if [ ! -f "target/game-price-tracker-1.0-SNAPSHOT.jar" ] || [ "src/" -nt "target/game-price-tracker-1.0-SNAPSHOT.jar" ]; then
    echo "JAR not found or source changed, compiling..."
    mvn clean package
else
    echo "Using existing JAR file..."
fi

echo "🚀 Starting application..."
java -jar target/game-price-tracker-1.0-SNAPSHOT.jar

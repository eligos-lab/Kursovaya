@echo off
echo Building Java application...
mvn clean package

echo Starting application...
java -jar target\game-price-tracker-1.0-SNAPSHOT.jar
pause

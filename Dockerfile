# Стадия 1: Сборка
FROM maven:3.8.4-openjdk-11 AS builder

WORKDIR /app

# Копируем pom.xml отдельно для кэширования зависимостей
COPY pom.xml .

# Скачиваем зависимости (кэшируется если pom.xml не менялся)
RUN mvn dependency:go-offline -B

# Копируем исходный код
COPY src ./src

# Собираем приложение и создаем JAR
RUN mvn clean package -DskipTests

# Стадия 2: Минимальный образ для запуска (опционально)
FROM openjdk:11-jre-slim

WORKDIR /app

# Копируем собранный JAR
COPY --from=builder /app/target/game-price-tracker-*.jar app.jar

# Создаем точку входа для JAR
ENTRYPOINT ["java", "-jar", "app.jar"]
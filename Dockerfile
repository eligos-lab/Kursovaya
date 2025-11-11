# Стадия 1: Сборка
FROM maven:3.8.4-openjdk-11 AS builder

WORKDIR /app

# Копируем pom.xml отдельно для кэширования зависимостей
COPY pom.xml .

# Скачиваем зависимости (кэшируется если pom.xml не менялся)
RUN mvn dependency:go-offline -B

# Копируем исходный код
COPY src ./src

# Собираем приложение
RUN mvn clean compile

# Стадия 2: Запуск (используем тот же образ)
FROM maven:3.8.4-openjdk-11

WORKDIR /app

# Копируем только собранные артефакты из стадии сборки
COPY --from=builder /app .

# Запускаем приложение
CMD ["mvn", "exec:java", "-Dexec.mainClass=com.gametracker.GamePriceTracker"]
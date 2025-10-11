# Используем базовый образ с JDK для сборки и запуска
FROM maven:3.9-eclipse-temurin-17 AS build

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем файлы проекта для сборки
COPY pom.xml .
COPY src ./src

# Собираем проект
RUN mvn clean package -DskipTests

# Используем легковесный JRE для финального образа
FROM eclipse-temurin:17-jre

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем собранный JAR из стадии сборки
COPY --from=build /app/target/*.jar app.jar

# Открываем порт приложения
EXPOSE 8080

# Команда для запуска Spring-приложения
ENTRYPOINT ["java", "-jar", "app.jar"]
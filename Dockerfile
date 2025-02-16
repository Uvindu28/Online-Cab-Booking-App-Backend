FROM maven:3.8.5-openjdk-23 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

FROM openjdk:23-jdk-slim
ARG JAR_FILE=target/*.jar
COPY --from=build /app/${JAR_FILE} app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]

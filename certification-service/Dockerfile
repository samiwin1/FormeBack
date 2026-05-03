# ── Stage 1: Build ────────────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn package -DskipTests -B

# ── Stage 2: Runtime ──────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY --from=build /app/target/certification-service-0.0.1-SNAPSHOT.jar app.jar

# Must match server.port in application.yml
EXPOSE 8090

ENTRYPOINT ["java", "-Dspring.profiles.active=docker", "-jar", "app.jar"]

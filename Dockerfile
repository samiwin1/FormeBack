# ─── Stage 1: Build ───────────────────────────────────────────────────────────
# Uses the full Maven + JDK image to compile and package the JAR.
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Copy pom first so Maven can download dependencies independently (layer cache).
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source and build the fat JAR, skipping tests (tests run in CI pipeline).
COPY src ./src
RUN mvn package -DskipTests -B

# ─── Stage 2: Runtime ─────────────────────────────────────────────────────────
# Lean JRE-only image for the final container.
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy only the packaged JAR from the build stage.
COPY --from=build /app/target/mentor-service-0.0.1-SNAPSHOT.jar app.jar

# Port must match server.port in application.yml.
EXPOSE 8088

ENTRYPOINT ["java", "-jar", "app.jar"]

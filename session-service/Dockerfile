FROM maven:3.9.6-eclipse-temurin-17-alpine AS build
WORKDIR /workspace/app

# Copy parent POM and ALL module POMs
COPY pom.xml .
COPY common/pom.xml common/
COPY user-service/pom.xml user-service/
COPY session-service/pom.xml session-service/
COPY api-gateway/pom.xml api-gateway/

# Prefetch dependencies
RUN mvn dependency:go-offline -B -pl common,session-service || true

# Copy source code for modules we need
COPY common/src common/src
COPY session-service/src session-service/src

# Build session-service and its dependencies
RUN mvn clean package -pl session-service -am -DskipTests

# --- Runtime stage ---
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /workspace/app/session-service/target/*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
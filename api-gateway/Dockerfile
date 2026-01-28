FROM maven:3.9.6-eclipse-temurin-17-alpine AS build
WORKDIR /workspace/app

# Copy parent POM and ALL module POMs
COPY pom.xml .
COPY common/pom.xml common/
COPY user-service/pom.xml user-service/
COPY session-service/pom.xml session-service/
COPY api-gateway/pom.xml api-gateway/

# Prefetch dependencies
RUN mvn dependency:go-offline -B -pl api-gateway || true

# Copy source code for api-gateway
COPY api-gateway/src api-gateway/src

# Build api-gateway
RUN mvn clean package -pl api-gateway -am -DskipTests

# --- Runtime stage ---
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /workspace/app/api-gateway/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
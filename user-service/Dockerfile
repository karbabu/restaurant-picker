# 1. Start with an image that already has Maven and Java 17
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build
WORKDIR /workspace/app

# 2. Copy parent POM and ALL module POMs (even if not building all)
COPY pom.xml .
COPY common/pom.xml common/
COPY user-service/pom.xml user-service/
COPY session-service/pom.xml session-service/
COPY api-gateway/pom.xml api-gateway/

# Prefetch dependencies
RUN mvn dependency:go-offline -B -pl common,user-service || true

# 3. Copy source code for modules we actually need
COPY common/src common/src
COPY user-service/src user-service/src

# 4. Build user-service and its dependencies
RUN mvn clean package -pl user-service -am -DskipTests

# --- Runtime stage ---
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /workspace/app/user-service/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
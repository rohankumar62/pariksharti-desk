FROM node:24-alpine AS frontend
WORKDIR /build/frontend
COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci --no-audit --no-fund
COPY frontend/ ./
RUN npm run build

FROM maven:3.9.11-eclipse-temurin-17 AS backend
WORKDIR /build/backend
COPY backend/pom.xml ./
RUN mvn -B -ntp dependency:go-offline
COPY backend/ ./
COPY --from=frontend /build/frontend/dist/ src/main/resources/static/
RUN mvn -B -ntp package

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN addgroup -S desk && adduser -S desk -G desk
COPY --from=backend --chown=desk:desk /build/backend/target/preparation-desk.jar app.jar
USER desk
ENV SPRING_PROFILES_ACTIVE=prod
EXPOSE 8080
CMD ["java", "-XX:MaxRAMPercentage=65.0", "-XX:InitialRAMPercentage=10.0", "-XX:+UseSerialGC", "-jar", "/app/app.jar"]

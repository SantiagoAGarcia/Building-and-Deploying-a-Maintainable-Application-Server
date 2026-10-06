# ==========================================
# Multi-stage Dockerfile for Mini Web Framework
# ==========================================

# Stage 1: Build stage with Maven and JDK 17
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder

WORKDIR /build

# Cache Maven dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build executable shaded JAR
COPY src ./src
RUN mvn clean package -DskipTests

# ==========================================
# Stage 2: Minimal Runtime stage with JRE 17
# ==========================================
FROM eclipse-temurin:17-jre-alpine

# Set non-sensitive default environment variables
ENV PORT=8080 \
    APP_ENV=production \
    GREETING_PREFIX=Hello

WORKDIR /app

# Create a secure, non-privileged user and group
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy the shaded executable JAR from the builder stage
COPY --from=builder /build/target/mini-web-framework-1.0.0.jar app.jar

# Ensure appropriate file permissions
RUN chown -R appuser:appgroup /app

# Switch to non-root user
USER appuser

# Expose default HTTP port
EXPOSE 8080

# Execute the application passing the environment PORT variable
ENTRYPOINT ["sh", "-c", "java -jar app.jar ${PORT}"]

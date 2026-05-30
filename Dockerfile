# --- Build stage ---
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace

# Copy Maven wrapper + pom first for dependency caching
COPY mvnw .
COPY .mvn/ .mvn/
COPY pom.xml .
RUN ./mvnw -q -DskipTests dependency:go-offline

# Copy source and build
COPY src/ src/
RUN ./mvnw -q -DskipTests package

# --- Runtime stage ---
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy the built jar (works for standard Spring Boot layout)
COPY --from=build /workspace/target/*.jar app.jar

# Optional: expose default Spring Boot port
EXPOSE 8080

# Optional JVM flags (tweak for memory-limited environments)
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]
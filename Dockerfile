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

# Copy the built jar
COPY --from=build /workspace/target/*.jar app.jar

# Expose default Spring Boot port
EXPOSE 8080

# Default low-memory JVM flags
ENV JAVA_OPTS="-XX:TieredStopAtLevel=1 -Xmx350m"

# Decode the Base64 variable into the local 'tokens/' directory before starting Java
ENTRYPOINT ["sh", "-c", "mkdir -p /app/tokens && echo \"$STORED_CREDENTIAL_BASE64\" | base64 -d > /app/tokens/StoredCredential && java $JAVA_OPTS -jar /app/app.jar"]
# --- Build stage ---
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Just copy everything and build it using global Maven
COPY . .
RUN mvn clean package -DskipTests

# --- Runtime stage ---
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENV JAVA_OPTS="-XX:TieredStopAtLevel=1 -Xmx350m"

ENTRYPOINT ["sh", "-c", "mkdir -p /app/tokens && echo \"$STORED_CREDENTIAL_BASE64\" | base64 -d > /app/tokens/StoredCredential && java $JAVA_OPTS -jar /app/app.jar"]
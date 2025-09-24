FROM maven:3.8.6-openjdk-18-slim AS build

WORKDIR /app

COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 mvn dependency:go-offline -B

COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn clean package -DskipTests -B

# Run stage
FROM openjdk:18-slim

WORKDIR /app

COPY --from=build /app/target/dialog-service-1.0.0.jar ./app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]

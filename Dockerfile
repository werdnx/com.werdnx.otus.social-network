# Build stage
FROM openjdk:18-slim AS build

# Install Maven
RUN apt-get update && \
    apt-get install -y maven && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Run stage
FROM openjdk:18-slim
WORKDIR /app
COPY --from=build /app/target/social-network-1.0.0.jar ./app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]

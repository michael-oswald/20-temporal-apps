# Build React frontend
FROM node:20 AS frontend-build
WORKDIR /app
COPY frontend/package*.json ./
RUN npm install
COPY frontend/ ./
RUN npm run build

# Build Spring Boot backend
# Build Spring Boot backend
FROM maven:3.9-eclipse-temurin-21 AS backend-build
WORKDIR /app
COPY pom.xml .
COPY src ./src
# Copy built frontend into Spring Boot static resources
COPY --from=frontend-build /app/build ./src/main/resources/static
RUN mvn clean package -DskipTests

# Run the app
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=backend-build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
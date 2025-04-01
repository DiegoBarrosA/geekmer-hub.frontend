# Use a Maven image to build the application
FROM maven:3.9-eclipse-temurin-17 AS build

# Set the working directory in the container
WORKDIR /app

# Copy the pom.xml file to download dependencies
COPY pom.xml .

# Download all dependencies (this step is cached if pom.xml doesn't change)
RUN mvn dependency:go-offline -B

# Copy the source code
COPY src/ ./src/

# Build the application
RUN mvn package -DskipTests

# Use Java runtime as a base image for the final image
FROM eclipse-temurin:17-jre-jammy

# Set the working directory in the container
WORKDIR /app

# Copy the JAR file from the build stage
COPY --from=build /app/target/*.jar app.jar

# Expose the port the app runs on
EXPOSE 8081

# Command to run the application
CMD ["java", "-jar", "app.jar"]

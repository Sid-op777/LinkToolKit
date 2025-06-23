# ---- Build Stage ----
# Use an official Maven image to build the application.
# This stage is named 'build'.
FROM maven:3.9-eclipse-temurin-21 AS build

# Set the working directory
WORKDIR /app

# Copy the POM file first to leverage Docker's layer caching.
# If the dependencies haven't changed, this layer won't be re-run.
COPY pom.xml .

# Download dependencies
RUN mvn dependency:go-offline

# Copy the rest of the source code
COPY src ./src

# Package the application into a JAR file
RUN mvn package -DskipTests


# ---- Run Stage ----
# Use a lightweight OpenJDK image to run the application.
FROM openjdk:21-jdk-slim

WORKDIR /app

# Copy only the built JAR file from the 'build' stage.
# The final image will not contain the Maven build tools or source code, making it smaller and more secure.
COPY --from=build /app/target/linktoolkit-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

# Set the entrypoint to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
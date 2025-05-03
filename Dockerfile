# Start with a lightweight OpenJDK image
FROM openjdk:21-jdk-slim

# Set working directory inside the container
WORKDIR /app

# Copy the jar file to the container
COPY target/linktoolkit-0.0.1-SNAPSHOT.jar app.jar

# Expose port (match your Spring Boot port if different)
EXPOSE 8080

# Run the jar file
ENTRYPOINT ["java", "-jar", "app.jar"]
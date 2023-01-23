# First stage: Build Maven Spring project
FROM maven:3.8.5-openjdk-17-slim AS maven_builder

# Set the working directory
WORKDIR /app

# Copy the project files to the working directory
COPY . .

# Build the Maven project
RUN mvn clean package -DskipTests

# Second stage: Build Go app
FROM golang:1.20.5-bullseye AS go_builder

# Install Go dependencies
RUN apt-get update && apt-get install libpcap-dev -y

# Set the working directory
WORKDIR /app

# Copy the project files to the working directory
COPY src/main/aaad_sniffer .

# Build the Go app
RUN go build .

# Third stage: Start the application
FROM eclipse-temurin:17-jre

# Set the working directory
WORKDIR /app

# Install Go dependencies
#RUN apt-get update && apt-get install libpcap-dev -y

# Copy the built artifact from the first stage
COPY --from=maven_builder /app/target/AnomalyDetector-0.0.1-SNAPSHOT.jar .

# Copy the built artifact from the second stage
COPY --from=go_builder /app/rules/aaad.rules .
COPY --from=go_builder /app/aaad-main .
COPY start.sh .

RUN chmod +x start.sh

# Expose the necessary port(s) for your application
EXPOSE 8080

# Start the application
CMD ["./start.sh"]
#CMD ["java", "-jar", "AnomalyDetector-0.0.1-SNAPSHOT.jar"]

# syntax=docker/dockerfile:1

# --- Build the Spring Boot jar ---
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY backend/.mvn .mvn
COPY backend/mvnw backend/pom.xml ./
RUN ./mvnw -B -q dependency:go-offline || true
COPY backend/src src
RUN ./mvnw -B -q -DskipTests package && cp target/backend-*.jar app.jar

# --- Runtime image with Ghidra bundled ---
FROM eclipse-temurin:21-jdk AS runtime
ARG GHIDRA_VERSION=11.4.3
ARG GHIDRA_ZIP=ghidra_11.4.3_PUBLIC_20251203.zip
RUN apt-get update \
 && apt-get install -y --no-install-recommends unzip wget ca-certificates \
 && rm -rf /var/lib/apt/lists/*
RUN wget -q "https://github.com/NationalSecurityAgency/ghidra/releases/download/Ghidra_${GHIDRA_VERSION}_build/${GHIDRA_ZIP}" -O /tmp/ghidra.zip \
 && unzip -q /tmp/ghidra.zip -d /opt \
 && rm /tmp/ghidra.zip \
 && mv /opt/ghidra_* /opt/ghidra \
 && sed -i "s#^JAVA_HOME_OVERRIDE=.*#JAVA_HOME_OVERRIDE=${JAVA_HOME}#" /opt/ghidra/support/launch.properties
ENV GHIDRA_HOME=/opt/ghidra
WORKDIR /app
COPY --from=build /app/app.jar app.jar
COPY backend/ghidra-scripts ghidra-scripts
EXPOSE 8080
# With ghidra.home set, revscope runs the real headless analyzer instead of the sample backend.
ENTRYPOINT ["java", "-jar", "app.jar", \
            "--ghidra.home=/opt/ghidra", \
            "--ghidra.script-path=/app/ghidra-scripts/ExportAnalysis.java"]

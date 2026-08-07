FROM maven:3.9-eclipse-temurin-17 AS build
# Set MAVEN_MIRROR_URL to build behind an internal Maven repository/proxy.
ARG MAVEN_MIRROR_URL=""
WORKDIR /workspace
RUN if [ -n "$MAVEN_MIRROR_URL" ]; then \
      mkdir -p /root/.m2 && printf '%s\n' \
        '<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0">' \
        '  <mirrors><mirror>' \
        '    <id>configured-mirror</id>' \
        "    <url>${MAVEN_MIRROR_URL}</url>" \
        '    <mirrorOf>central</mirrorOf>' \
        '  </mirror></mirrors>' \
        '</settings>' > /root/.m2/settings.xml; \
    fi
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/target/inventory-backend-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

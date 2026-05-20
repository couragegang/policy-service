FROM gradle:8.10.2-jdk21 AS build
WORKDIR /app
COPY gradle.properties settings.gradle.kts build.gradle.kts gradlew gradlew.bat ./
COPY gradle ./gradle
COPY src ./src
RUN gradle --no-daemon shadowJar -x test \
    && JAR=$(ls build/libs/*-all.jar | head -n1) \
    && cp "$JAR" /app/policy-service.jar

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/policy-service.jar /app/policy-service.jar
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "/app/policy-service.jar"]

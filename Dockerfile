FROM eclipse-temurin:17-jdk

COPY target/gym-api-0.0.1.jar /app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app.jar"]

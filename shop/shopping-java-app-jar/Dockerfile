FROM eclipse-temurin:17-jre

WORKDIR /app

COPY target/shopping.jar /app/shopping.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/shopping.jar"]

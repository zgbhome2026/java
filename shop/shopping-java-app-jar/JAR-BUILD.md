# Build executable JAR

This project is configured as a Spring Boot executable JAR.

## If Maven is installed

```bash
cd shopping-java-app-jar
mvn clean package -DskipTests
```

Output:

```text
target/shopping.jar
```

Run it:

```bash
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres-svc:5432/shoppingdb \
SPRING_DATASOURCE_USERNAME=admin \
SPRING_DATASOURCE_PASSWORD=secretpassword \
java -jar target/shopping.jar
```

## If Maven is not installed but Docker is available

```bash
docker run --rm \
  -v "$PWD":/app \
  -w /app \
  maven:3.9.9-eclipse-temurin-17 \
  mvn clean package -DskipTests
```

The JAR will be created as:

```text
target/shopping.jar
```

## Build a container directly

```bash
docker build -t java-shopping-app:latest .
```

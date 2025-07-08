# Spring boot API sandbox

I created this Java Spring project to level up with that techno by creating a simple API.

## 1. Prerequisites

You need to install ``maven``, ``Java 17`` and ``Docker``.

## 2. Launching dev environment

How to launch the dev environment locally, on port `8080`, with a PostgreSQL database on port `5432`.

> NB: Swagger UI will be available at http://localhost:8080/api/swagger-ui/index.html.

### On Linux

The following command will start a PostgreSQL database in a Docker container and launch the Spring Boot API using maven:
```bash
./run.sh
```

### On Windows

The following command will start a PostgreSQL database in a Docker container and launch the Spring Boot API using maven:
```bash
run.bat
```

### Using IntelliJ

First, you need to start a PostgreSQL database in a Docker container

```bash
docker run --name spring-api-db-postgres -e POSTGRES_PASSWORD=Password#1 -d -p 5432:5432 postgres
```

Then, you can run the project using `main` method in the `ApiApplication` class, and by adding `-Dspring.profiles.active=dev` to run configuration.

## 3. Launching tests

Integration tests can be run using:
```bash
mvn test
```

## 4. Launching production environment

First, you need to create a `.env` file at the root of the project with the following environment variables:
```
JWT_SECRET=
POSTGRES_PASSWORD=
ADMIN_PASSWORD=
```

Then, you need to build the project using:
```bash
mvn clean package -DskipTests
```

Finnally, you can build and deploy the containerized API using:
```bash
docker-compose up -d --build
```

## 5. TODOs

- Add nginx as a load balancer + manage HTTPS ?
- Add log mechanism
- Write documentation (user doc, dev doc, api/tech doc)
- Create a CI pipeline on GitHub
- Fix Spring runtime warnings

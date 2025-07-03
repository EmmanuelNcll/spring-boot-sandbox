#!/bin/bash

cd "$(dirname "$0")"

echo "Starting PostgreSQL Docker container..."
if ! docker ps -q --filter "name=spring-api-db-postgres" | grep -q .; then
  docker run --name spring-api-db-postgres -e POSTGRES_PASSWORD=Password#1 -d -p 5432:5432 postgres
fi

echo Building and starting the Spring Boot application...
mvn clean spring-boot:run -Dspring-boot.run.profiles=dev -DskipTests

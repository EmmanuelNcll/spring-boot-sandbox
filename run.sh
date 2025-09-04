#!/bin/bash

cd "$(dirname "$0")"

is_container_running() {
  docker ps -q --filter "name=spring-api-db-postgres" --filter "status=running" >/dev/null
  return $?
}

echo "Starting PostgreSQL Docker container..."
if ! is_container_running; then
  docker run --name spring-api-db-postgres -e POSTGRES_PASSWORD=Password#1 -d -p 5432:5432 postgres
  sleep 5
  
  if ! is_container_running; then
    echo "Error: Failed to start PostgreSQL Docker container."
    exit 1
  fi
fi

echo Building and starting the Spring Boot application...
mvn clean spring-boot:run -Dspring-boot.run.profiles=dev -DskipTests

@echo off

cd %~dp0

echo Starting PostgreSQL Docker container...
docker ps -q --filter "name=spring-api-db-postgres" | findstr . >nul
if errorlevel 1 (
    docker run --name spring-api-db-postgres -e POSTGRES_PASSWORD=Password#1 -d -p 5432:5432 postgres
)

echo Building and starting the Spring Boot application...
mvn clean spring-boot:run -Dspring-boot.run.profiles=dev -DskipTests

pause

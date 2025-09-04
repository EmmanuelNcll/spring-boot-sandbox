@echo off

cd %~dp0

echo Starting PostgreSQL Docker container...
call :is_container_running
if errorlevel 1 (
    docker run --name spring-api-db-postgres -e POSTGRES_PASSWORD=Password#1 -d -p 5432:5432 postgres
    timeout /t 5 >nul

    call :is_container_running
    if errorlevel 1 (
        echo Error: Failed to start PostgreSQL Docker container.
        pause
        exit /b 1
    )
)

echo Building and starting the Spring Boot application...
mvn clean spring-boot:run -Dspring-boot.run.profiles=dev -DskipTests

pause

:is_container_running
docker ps -q --filter "name=spring-api-db-postgres" --filter "status=running" | findstr . >nul
exit /b %errorlevel%
# Notification System (Backend)

This is a Java Spring Boot application that manages a notification system. It handles message categorization, user subscriptions, and delivery history.

## Prerequisites

- **Java 17** or higher
- **Maven** (or use the included `./mvnw`)
- **Docker** and **Docker Compose** (for the database)

## Getting Started

### 1. Start the Database

The application requires a MySQL database. You can start it using the provided Docker Compose file:

```bash
docker-compose up -d
```

This will start a MySQL instance on port `3306` with the following credentials:
- **Database**: `notification_db`
- **User**: `user`
- **Password**: `password`

### 2. Run the Application

Once the database is running, you can start the Spring Boot application:

```bash
./mvnw spring-boot:run
```

The application will be available at `http://localhost:8080`.

## API Documentation

- **Base URL**: `http://localhost:8080/api`
- **H2 Console** (if enabled for debugging): `http://localhost:8080/h2-console`

## Key Features

- **SOLID Principles**: Clean architecture and design patterns.
- **Strategy Pattern**: Used for handling different notification channels (SMS, Email, Push).
- **Database Migrations**: Managed by Flyway.
- **Validation**: Input validation using Spring Validation.

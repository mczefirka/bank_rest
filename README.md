# Bank Card Management System

Backend Java (Spring Boot) application for managing bank cards.

## Technologies

- Java 21, Spring Boot 4.1, Spring Security, Spring Data JPA
- PostgreSQL, Liquibase
- JWT (jjwt 0.12.6)
- MapStruct, Lombok
- Swagger/OpenAPI (springdoc 2.7.0)
- Docker Compose
- JUnit 5 + Mockito

## Requirements

- JDK 21+
- Docker + Docker Compose (for database)
- Maven (or `mvnw` wrapper)

## Quick Start

### 1. Start PostgreSQL

```bash
docker compose up -d postgres
```

### 2. Build and run the application

```bash
mvn clean package -DskipTests
java -jar target/bankcards-0.0.1-SNAPSHOT.jar
```

Or fully via Docker:

```bash
docker compose up --build
```

### 3. Access

| Resource | URL |
|---|---|
| Swagger UI | http://localhost:8080/swagger-ui.html |
| API | http://localhost:8080/api |

### 4. Credentials

An admin user is created on startup:

- Email: `admin@bank.com`
- Password: `password`

## API endpoints

### Auth

| Method | Path | Description |
|---|---|---|
| POST | `/api/auth/login` | Get JWT token |

### Users (ADMIN only)

| Method | Path | Description |
|---|---|---|
| POST | `/api/admin/users` | Create a user |
| GET | `/api/admin/users` | List all users |
| GET | `/api/admin/users/{id}` | Get user by ID |
| GET | `/api/admin/users/by-email?email=` | Get user by email |
| DELETE | `/api/admin/users/{id}` | Delete a user |

### Cards (USER)

| Method | Path | Description |
|---|---|---|
| GET | `/api/cards` | List own cards (paginated + filtered) |
| GET | `/api/cards/{id}` | Get card details |
| GET | `/api/cards/{id}/balance` | Get card balance |
| POST | `/api/cards/transfer` | Transfer between own cards |
| POST | `/api/cards/{id}/block` | Block own card |

### Cards (ADMIN)

| Method | Path | Description |
|---|---|---|
| POST | `/api/admin/cards` | Create a card |
| GET | `/api/admin/cards` | List all cards (paginated + filtered) |
| GET | `/api/admin/cards/{id}` | Get any card details |
| PUT | `/api/admin/cards/{id}` | Update a card |
| DELETE | `/api/admin/cards/{id}` | Delete a card |
| PATCH | `/api/admin/cards/{id}/block` | Block a card |
| PATCH | `/api/admin/cards/{id}/activate` | Activate a card |

## Security

- JWT token is passed via `Authorization: Bearer <token>` header
- Roles: `ADMIN` and `USER`
- Card numbers are not stored in plain text — SHA-256 hash + last 4 digits are used
- Passwords are hashed with BCrypt
- All API endpoints (except `/api/auth/*` and Swagger) are protected by JWT filter

## Project Architecture

```
src/main/java/com/example/bankcards/
├── config/          # Security, Swagger configuration
├── controller/      # REST controllers
├── dto/
│   ├── request/     # Request DTOs
│   └── response/    # Response DTOs
├── entity/          # JPA entities
├── exception/       # Custom exceptions + GlobalExceptionHandler
├── mapper/          # MapStruct mapper
├── repository/      # Spring Data JPA repositories
├── security/        # JWT provider, filter, UserDetailsService
├── service/         # Business logic
└── util/            # Utilities (card number hashing, Luhn validation)
```

## Tests

```bash
mvn test
```

Core business logic is covered by unit tests (JUnit 5 + Mockito).

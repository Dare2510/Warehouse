# Warehouse Management System (Spring Boot Backend)

A Spring Boot backend application for managing warehouse operations,
including product management, inventory tracking, order processing,
and role-based access control.

## Features

1. Product creation, update, and validation
2. Order creation and cancellation
3. Inventory creation for stock creation and overview
4. Location management for product storage
5. Automatic stock updates when orders are created or canceled
6. Order status management (e.g. `ORDER_PLACED`, `SHIPPED`)
7. Global exception handling with structured error responses
8. Custom domain exceptions (e.g. `DuplicateProductException`, `OrderNotFoundException`)
9. PostgreSQL database integration using JPA/Hibernate
10. JWT authentication with role-based authorization (`USER`, `CLERK`, `ADMIN`)
11. Unit and integration tests
12. Docker / Docker Compose support

## Tech Stack

* Java
* Spring Boot
* Spring Data JPA / Hibernate
* PostgreSQL
* Maven
* JUnit / Mockito
* JWT Security
* Swagger
* Docker

## Architecture

The application follows a layered architecture:

`Controller -> Security -> Service -> Repository -> Database`

### Additional Components

- GlobalExceptionHandler
- ErrorResponse
- JWT filter
- Custom domain exceptions
- Jakarta Bean Validation

## Security

Authentication is handled using JWT tokens.

### Roles

- `USER` – can create and cancel orders
- `CLERK` – can manage products and update order status
- `ADMIN` – full system access

### Users
currently is no user creation supported, users are created via InMemoryUserDetails 
in `AppUserConfig.class`

## Database

The application uses PostgreSQL with:

- `spring.jpa.hibernate.ddl-auto=update`

### Main Relationships

- `OrderEntity -> ProductEntity` (`ManyToOne`)
- `LocationEntity -> ProductEntity` (`ManyToOne`)
- `InventoryEntity -> LocationEntity` (`OneToOne`)
- `InventoryEntity -> ProductEntity` (`ManyToOne`)

## Configuration

The application uses the following environment variables:

```
- DB_HOST
- DB_NAME
- DB_USERNAME
- DB_PASSWORD

- JWT_SECRE`
- JWT_EXPIRATION_MS

### Example `.env`
```

```env
DB_HOST=postgres
DB_NAME=warehouse
DB_USERNAME=warehouse
DB_PASSWORD=warehouse

JWT_SECRET=replace-with-a-long-secret
JWT_EXPIRATION_MS=3600000
```


**Running locally**

Make sure PostgreSQL is running and that a database named warehouse exists.

Create a .env file based on .env.example, then run:
```
./mvnw spring-boot:run
```
**Running with Docker Compose**

From the docker directory run:
```
docker compose --env-file ../.env up --build
```
This starts:

PostgreSQL mapped to host port 54433
Spring Boot application mapped to host port 8081

The application connects to PostgreSQL internally via:

```jdbc:postgresql://postgres:5432/cinema```

## Testing

The project includes:

* controller tests using MockMvc
* service layer tests
* security flow tests
* validation tests

**Run tests with:**
```
./mvnw test
```
## Learning Goals

**This project was built to:**

* Deepen understanding of Spring Boot architecture
* Implement JWT-based security from scratch
* Work with JPA entity relationships
* Implement centralized error handling
* Improve testing strategy
* Improve code quality
* Learn Docker
* Practice clean commit structure and incremental improvements


## Known limitations

The project uses ddl-auto=update instead of database migrations
The focus is on backend business logic and API design
JWT authentication is implemented, but the project is not positioned as a production-ready deployment template

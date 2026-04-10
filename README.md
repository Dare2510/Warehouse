# Warehouse Management System (Spring Boot Backend)

A Spring Boot backend application for managing warehouse operations,
including product management, inventory tracking, order processing,
and role-based access control. 

## Features

 1. Product creation, update, and validation
 2. Order creation and cancellation
 3. Inventory creation for stock creation and overview
 4. Location service, for stock storing
 5. Automatic stock management (creating/cancelling an order decreases/increases stock on Locations and inventory)
 6. Order status management (e.g. ORDER_PLACED, SHIPPED)
 8. Global exception handling with structured error responses
 9. Custom domain exceptions (e.g. DuplicateProductException, OrderNotFoundException)
 10. MySQL database integration using JPA/Hibernate
 11. JWT authentication with role-based authorization (USER, CLERK, ADMIN)
 12. Unit and integration tests

## Tech Stack

- Java 17
- Spring Boot
- Spring Security (JWT)
- MySQL
- Docker / Docker Compose     

## Architecture

  The application follows a clean layered architecture:

    Controller → Security Layer(JWT) → Service → Repository → Database
    
 ### Additional Components
  1. GlobalExceptionHandler
  2. ErrorResponse DTO
  3. JWT Security Filter
  4. Custom domain exceptions
  5. Validation using Jakarta Bean Validation

## Security

  Authentication is handled using JWT tokens.
  
###  Roles
  1. USER – can create and cancel orders
  2. CLERK – can manage products and update order status
  3. ADMIN – full system access

  JWT configuration is externalized via environment variables.

## Database

###  MySQL
  hibernate.ddl-auto=update
  
  ### Relationships

  
  OrderEntity -> ProductEntity
  (ManyToOne)

  
  LocationEntity -> ProductEntity
  (ManyToOne)

  
  InventoryEntity -> LocationEntity
  (OneToOne)

  
  InventoryEntity -> ProductEntity
  (ManyToOne)

## Configuration

  The application uses the following environment variables:

    DB_URL
    DB_USERNAME
    DB_PASSWORD
    JWT_SECRET
    JWT_EXPIRATION_MS

### Example (local setup)

    DB_URL=jdbc:mysql://localhost:3306/warehouse
    DB_USERNAME=warehouse
    DB_PASSWORD=warehouse
    JWT_SECRET=your-very-long-secret-key
    JWT_EXPIRATION_MS=3600000

### Example (docker setup)

    DB_URL=jdbc:mysql://db:3306/warehouse
    DB_USERNAME=warehouse
    DB_PASSWORD=warehouse
    JWT_SECRET=your-very-long-secret-key
    JWT_EXPIRATION_MS=3600000

## Running the Application 

### local:


  Make sure MySQL is running
  Set the required environment variables
  
  Run:

    ./mvnw spring-boot:run

  or start the application via your IDE.
  
### via docker:


  Make sure Docker Desktop is running
  Set the required environment variables, if needed make changes in the Docker-compose.yml file

  Run:
  
    cd docker
    ./docker compose up -d


## Testing

  The project includes:
  1. Controller tests using MockMvc
  2. Service layer tests
  3. Security flow tests
  4. Validation tests
  Run tests with:

    ./mvnw test

## Learning Goals

  This project was built to:
  1. Deepen understanding of Spring Boot architecture
  2. Implement JWT-based security from scratch
  3. Work with JPA entity relationships
  4. Implement structured and centralized error handling
  5. Improve testing strategy (unit & integration testing)
  6. Improve code quality
  7. Learn Docker
  8. Practice clean commit structure and incremental improvements
  





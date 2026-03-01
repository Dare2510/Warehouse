📦 Warehouse Simulation

  A simple Spring Boot backend application that simulates a warehouse management system.

  The system provides product management, order processing, automatic stock handling, and role-based access control using JWT authentication. 

🚀 Features

 1. Product creation, update, and validation
 2. Order creation and cancellation
 3. Automatic stock management (creating an order decreases stock)
 4. Order status management (e.g. ORDER_PLACED, SHIPPED)
 5. Global exception handling with structured error responses
 6. Custom domain exceptions (e.g. DuplicateProductException, OrderNotFoundException)
 7. MySQL database integration using JPA/Hibernate
 8. JWT authentication with role-based authorization (USER, CLERK, ADMIN)
 9. Unit and integration tests
     

🏗 Architecture

  The application follows a clean layered architecture:

    Controller → Service → Repository → Database
    
  Additional Components
  1. GlobalExceptionHandler
  2. ErrorResponse DTO
  3. JWT Security Filter
  4. Custom domain exceptions
  5. Validation using Jakarta Bean Validation

🔐 Security

  Authentication is handled using JWT tokens.
  
  Roles
  1. USER – can create and cancel orders
  2. CLERK – can manage products and update order status
  3. ADMIN – full system access

  JWT configuration is externalized via environment variables.

🗄 Database

  MySQL
  hibernate.ddl-auto=update
  
  Relationships
  OrderEntity → ProductEntity
  (ManyToOne)

⚙ Configuration

  The application uses the following environment variables:

    DB_URL
    DB_USERNAME
    DB_PASSWORD
    JWT_SECRET
    JWT_EXPIRATION_MS

Example (local setup)

    DB_URL=jdbc:mysql://localhost:3306/warehouse
    DB_USERNAME=warehouse
    DB_PASSWORD=warehouse
    JWT_SECRET=your-very-long-secret-key
    JWT_EXPIRATION_MS=3600000

▶ Running the Application

  Make sure MySQL is running
  Set the required environment variables
  
  Run:

    ./mvnw spring-boot:run

  or start the application via your IDE.

🧪 Testing

  The project includes:
  1. Controller tests using MockMvc
  2. Service layer tests
  3. Security flow tests
  4. Validation tests
  Run tests with:

    ./mvnw test

🎯 Learning Goals

  This project was built to:
  1. Deepen understanding of Spring Boot architecture
  2. Implement JWT-based security from scratch
  3. Work with JPA entity relationships
  4. Implement structured and centralized error handling
  5. Improve testing strategy (unit & integration testing)
  6. Practice clean commit structure and incremental improvements
  





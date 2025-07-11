# Gym Management API

This API is designed to manage gym clients and memberships, providing role-specific functionalities with security and full control over operations.

## Features

- **User and Membership Management:** Users can create an account by providing their details such as full-name, dni and password.
- **Authentication and Authorization:** JWT-based authentication is implemented to secure user access.
- **Role-Based Access Control:**
  - **ADMIN:** Full system control, including managing users, staff, memberships, and access to logs.
  - **STAFF:** Manage and track clients and memberships, with permissions limited to update and read operations.
  - **USER:** Self-registration and management of personal data and memberships, with te ability to pay membership fees online.
- **Payment Management:** Users can pay their memberships directly through the application without needing to vising the gym.
- **Auditing and Logs:** Tracking of movements and operations within the API to maintain transparency and security.


## Technologies Used

The project is built using the following technologies:

- **Java 17 + Spring Boot** - Backend framework
- **Spring Security** - JWT-based authentication and role management
- **Spring Data JPA + Hibernate** -  ORM and relational DB layer
- **Bean Validation (Hibernate Validator)** - Input validation
- **Maven** - for dependency and build management
- **Swagger / OpenAPI** - for automatic API documentation and testing
- **JUnit, Mockito, MockMvc** - Unit and integration testing
- **SLF4J + Logback + MDC** - Structured logging and audit trail
- **Spring Boot Actuator + Micrometer** - System metrics and monitoring
- **Docker (planned)** - Containerization for deployment (in progress)

## API Endpoints

The application exposes the following RESTful API endpoints:

- `/api/v1/auth/register` - Register a new user account.
- `/api/v1/auth/login` - User login to obtain JWT token.
- `/api/v1/auth/logout` - Logout user and clear the security context.

- `/api/v1/user/**` - View or update personal user information.
- `/api/v1/user/membership/**` - View membership details or make payments.


- `/api/v1/staff/user/**` - View or update user data (limited to staff permissions).
- `/api/v1/staff/membership/**` - Generate, view or update memberships for existing clients.


- `/api/v1/admin/user/**` - Full CRUD operations on users and staff.
- `/api/v1/admin/membership/**` - Full CRUD operations on memberships.

## Deployment using Docker

The application has been Dockerized for easier deployment and management. Below are the steps to deploy the application using Docker:

### Prerequisites

Ensure that Docker is installed on your machine.

### Instructions

This will start the Fitness Tracking Application, including the Spring Boot backend, MongoDB database, and Mongo Express for managing the database. Access the application at `http://localhost:8080`.




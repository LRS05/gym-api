# Gym Management API

This API is designed to manage gym clients and memberships, providing role-specific functionalities with security and full control over operations.

## Features

- **User and Membership Management:** Users can create an account by providing their details such as full name, dni and password.
- **Authentication and Authorization:** JWT-based authentication is implemented to secure user access.
- **Role-Based Access Control:**
  - **ADMIN:** Full system control, including managing users, staff, memberships, and access to logs.
  - **STAFF:** Manage and track clients and memberships, with permissions limited to update and read operations.
  - **USER:** Self-registration and management of personal data and memberships, with the ability to pay membership fees online.
- **Auditing and Logs:** Tracking of movements and operations within the API to maintain transparency and security.
- **Notifications:** WhatsApp Notifications for membership expiration or payment confirmations.
- **Testing:** Fully covered with unit, integration and parameterized tests for cleaner and more maintainable test code.


## Technologies Used

The project is built using the following technologies:

- **Java 17 + Spring Boot** - Backend framework
- **Spring Security** - JWT-based authentication with cookie support and role management
- **Spring Data JPA + Hibernate** - ORM and relational DB layer
- **Bean Validation (Hibernate Validator)** - Input validation
- **Maven** - for dependency and build management
- **Swagger / OpenAPI** - for automatic API documentation and testing
- **JUnit, Mockito, MockMvc** - Unit and integration testing
- **SLF4J + Logback + MDC** - Structured logging and audit trail
- **Spring Boot Actuator + Micrometer** - System metrics and monitoring
- **Docker + Docker Compose** - Containerization for deployment (API + MySQL database)
- **WhatsApp notifications** - Automated notifications for membership expiration and payment confirmations

## API Endpoints

The application exposes the following RESTful API endpoints:

### Authentication

- `/api/v1/auth/register` - Register a new user account.
- `/api/v1/auth/login` - User login to obtain JWT token.
- `/api/v1/auth/logout` - Logout user and clear the security context.

### User Endpoints

- `/api/v1/user/**` - View or update personal user information.
- `/api/v1/user/membership/**` - View membership details or make payments.

### Staff Endpoints

- `/api/v1/staff/user/**` - View or update user data (limited to staff permissions).
- `/api/v1/staff/membership/**` - Generate, view or update memberships for existing clients.


### Admin Endpoints

- `/api/v1/admin/user/**` - Full CRUD operations on users and staff.
- `/api/v1/admin/membership/**` - Full CRUD operations on memberships.

## Deployment using Docker

The application is Dockerized with Docker Compose for easier deployment, including the Spring Boot API and MySQL database.

### Prerequisites

Docker and Docker Compose installed on your machine.

### Instructions
1. Clone the repository
```sh
git clone https://github.com/github_username/gym-api.git
cd gym-management-api
```

2. Start the application and database using Docker Compose:
```sh
docker compose up --build
```

3. The API will be accessible at `http://localhost:8080` and MySQL will be running on the configured port 3306.


4. Access Swagger UI for testing and API exploration at:
```sh
http://localhost:8080/swagger-ui.html
```

### Notes

- Database credentials and configuration are managed in the docker-compose.yml file and application.properties.
- To stop the application and database, run:
```sh
docker compose down
```





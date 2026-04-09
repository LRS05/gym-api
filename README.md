# 🏋️ Gym Management API

The API built with Spring Boot / MySQL manages gym members and memberships with secure role-based access control.

## 🚀 Features

- **User registration and authentication with JWT**
- **Role-based access control (ADMIN, STAFF, USER)**
- **Membership creation and management**
- **WhatsApp notifications for membership expiration and payments**
- **Structured logging and auditing**
- **API documentation with Swagger / OpenAPI**
- **Monitoring with Spring Boot Actuator**
- **Unit and Integration testing with JUNit, Mockito and RestTestClient**
- **Docker containerization with Docker Compose**
- **CI with GitHub Actions**
- **CD with Docker image build and push**

## 🧰 Technologies Used

The project is built using the following technologies:

- **Java 17**
- **Spring Boot 4**
- **Spring Security**
- **Spring Data JPA  + Hibernate**
- **MySQL (development)**
- **PostgreSQL (production)**
- **Docker**
- **Docker Compose**
- **GitHub Actions**
- **Spring Boot Actuator**
- **Micrometer**
- **SLF4J + Logback + MDC**
- **JUnit**
- **Mockito**
- **RestTestClient**
- **Swagger / OpenAPI**
- **Maven**
- **WhatsApp API**

## 🔗 API Endpoints

### 🔐 Authentication

- `POST /api/v1/auth/register` → Register a new user
- `POST /api/v1/auth/login` → Authenticate user and obtain JWT cookies
- `POST /api/v1/auth/refresh` → Refresh access token
- `POST /api/v1/auth/logout` → Logout user

---

### 👤 User

- `/api/v1/user/**` → Manage personal user data
- `/api/v1/user/memberships/**` → Access memberships

---

### 🧑‍💼 Staff (limited access)

- `/api/v1/staff/users/**` → Manage users
- `/api/v1/staff/users/dni/{dni}/memberships/**` → Manage user memberships
- `/api/v1/staff/users/memberships/**` → Manage memberships

---

### 🛠️ Admin (full access)

- `/api/v1/admin/users/**` → Full CRUD on users and staff
- `/api/v1/admin/users/dni/{dni}/memberships/**` → Full CRUD on user memberships
- `/api/v1/admin/users/memberships/**` → Full CRUD on memberships

---

### 📊 Metrics & Documentation

- `/swagger-ui/index.html` → Swagger UI
- `/actuator` → List of exposed actuator endpoints

---

## 🐳 Deployment using Docker

The application is dockerized with Docker Compose for easier deployment, including the Spring Boot API and MySQL database.

### ⚙️ Prerequisites

Docker and Docker Compose installed on your machine.

### 📦 Instructions

1_ Clone the repository
```sh
git clone https://github.com/LRS05/gym-api.git  
cd gym-api  
```

2_ Configure environment variables
```
.env.example -> .env
(database, JWT, WhatsApp API)
```

3_ Start the application
```sh
docker compose up --build  
```  
4_ Access services
- API -> http://localhost:8080
- Swagger -> http://localhost:8080/swagger-ui/index.html
- MySQL -> port 3306

### Notes

- To stop the application and database, run:
```sh
docker compose down  
```

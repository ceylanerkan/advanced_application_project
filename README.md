# 🛒 E-Commerce Analytics Platform — Backend API

A **Spring Boot 4.0.3** REST API backend for a multi-role e-commerce platform with JWT authentication, role-based access control (RBAC), and an integrated Multi-Agent Text2SQL AI chatbot powered by LangGraph.

> Built with Java 21 · Spring Security · JPA/Hibernate · MySQL · Swagger/OpenAPI

---

## 📑 Table of Contents

- [Features](#-features)
- [Tech Stack](#-tech-stack)
- [Architecture](#-architecture)
- [Getting Started](#-getting-started)
- [Configuration](#-configuration)
- [API Endpoints](#-api-endpoints)
- [Authentication Flow](#-authentication-flow)
- [Role-Based Access Control](#-role-based-access-control)
- [AI Chatbot Module](#-ai-chatbot-module)
- [Project Structure](#-project-structure)
- [Running Tests](#-running-tests)

---

## ✨ Features

- **JWT Authentication** — Stateless auth with access + refresh token support
- **Three-Tier RBAC** — `ADMIN`, `CORPORATE`, and `INDIVIDUAL` roles with fine-grained permissions
- **Full CRUD APIs** — Users, Products, Orders, Order Items, Categories, Reviews, Shipments, Stores, Customer Profiles
- **Admin Dashboard API** — Dedicated admin endpoints for platform-wide management
- **AI Chatbot Integration** — Multi-Agent Text2SQL chatbot via a Python/LangGraph microservice
- **Swagger UI** — Interactive API documentation at `/swagger-ui.html`
- **Input Validation** — Jakarta Bean Validation on all request bodies
- **Global Exception Handling** — Centralized error responses via `@ControllerAdvice`
- **SQL Injection Defense** — JSqlParser-based AST validation for AI-generated queries
- **Multi-Database Support** — Spring profiles for MySQL and PostgreSQL

---

## 🧰 Tech Stack

| Layer | Technology |
|---|---|
| Framework | Spring Boot 4.0.3 |
| Language | Java 21 |
| Security | Spring Security + JWT (jjwt 0.11.5) |
| ORM | Spring Data JPA / Hibernate |
| Database | MySQL 8+ (default) / PostgreSQL |
| Validation | Jakarta Bean Validation |
| API Docs | SpringDoc OpenAPI 3.0.3 (Swagger UI) |
| Build | Maven |
| AI Module | Python 3 + LangGraph + Gemini |
| Code Gen | Lombok |

---

## 🏗 Architecture

```
┌──────────────────────────────────────────────────────────────┐
│                        Client (Frontend)                     │
└────────────────────────────┬─────────────────────────────────┘
                             │ HTTP + JWT Bearer Token
┌────────────────────────────▼─────────────────────────────────┐
│                    Spring Boot Backend                        │
│  ┌──────────────┐  ┌──────────────┐  ┌────────────────────┐  │
│  │  Controllers  │→│   Services   │→│   Repositories     │  │
│  │  (REST API)   │  │  (Business)  │  │  (Spring Data JPA) │  │
│  └──────────────┘  └──────────────┘  └────────┬───────────┘  │
│         ↑                                      │              │
│  ┌──────┴───────┐                    ┌────────▼───────────┐  │
│  │  JWT Filter   │                    │     MySQL / PG     │  │
│  │  Security     │                    │     Database       │  │
│  └──────────────┘                    └────────────────────┘  │
│         │                                                     │
│  ┌──────▼───────────────────────────────────────────────┐    │
│  │  AI Communication Service → Python LangGraph Agent   │    │
│  └──────────────────────────────────────────────────────┘    │
└──────────────────────────────────────────────────────────────┘
```

---

## 🚀 Getting Started

### Prerequisites

- **Java 21** (JDK)
- **Maven 3.9+**
- **MySQL 8+** (or PostgreSQL 14+)
- **Python 3.10+** (for AI chatbot module, optional)

### 1. Clone the Repository

```bash
git clone https://github.com/<your-username>/ecommerce-analytics-platform.git
cd ecommerce-analytics-platform
```

### 2. Create the Database

```sql
CREATE DATABASE advanced_project;
```

### 3. Configure Application Properties

Edit `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/advanced_project
spring.datasource.username=root
spring.datasource.password=your_password
```

> **⚠️ Important:** Update the JWT secret key for production use.

### 4. Build & Run

```bash
# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

The API will start at **`http://localhost:8080`**.

### 5. Access Swagger UI

Open your browser and navigate to:

```
http://localhost:8080/swagger-ui.html
```

---

## ⚙ Configuration

### Application Properties

| Property | Default | Description |
|---|---|---|
| `spring.datasource.url` | `jdbc:mysql://localhost:3306/advanced_project` | Database connection URL |
| `spring.jpa.hibernate.ddl-auto` | `update` | Auto-create/update schema |
| `application.security.jwt.secret-key` | (set in props) | HMAC-SHA256 signing key (Base64) |
| `application.security.jwt.expiration` | `900000` | Access token TTL (15 min) |
| `application.security.jwt.refresh-token.expiration` | `604800000` | Refresh token TTL (7 days) |

### Spring Profiles

| Profile | Database | Activate With |
|---|---|---|
| (default) | MySQL | — |
| `mysql` | MySQL | `--spring.profiles.active=mysql` |
| `postgres` | PostgreSQL | `--spring.profiles.active=postgres` |

---

## 📡 API Endpoints

### Authentication (Public — No Token Required)

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/auth/register` | Register a new user |
| `POST` | `/api/auth/login` | Login and receive JWT tokens |
| `POST` | `/api/auth/refresh-token` | Refresh an expired access token |

### Users

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `GET` | `/api/users` | ADMIN | List all users |
| `GET` | `/api/users/{id}` | Owner / ADMIN | Get user by ID |
| `PUT` | `/api/users/{id}` | Owner / ADMIN | Update user profile |
| `DELETE` | `/api/users/{id}` | Owner / ADMIN | Delete user account |

### Products

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `GET` | `/api/products` | All | List all products |
| `GET` | `/api/products/{id}` | All | Get product by ID |
| `POST` | `/api/products` | ADMIN, CORPORATE | Create a product |
| `PUT` | `/api/products/{id}` | ADMIN, CORPORATE | Update a product |
| `DELETE` | `/api/products/{id}` | ADMIN, CORPORATE | Delete a product |

### Orders

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `GET` | `/api/orders` | Scoped | List orders (own / all) |
| `GET` | `/api/orders/{id}` | Owner / ADMIN | Get order by ID |
| `POST` | `/api/orders` | All authenticated | Create an order |
| `PUT` | `/api/orders/{id}` | Owner / ADMIN | Update an order |
| `DELETE` | `/api/orders/{id}` | ADMIN, CORPORATE | Delete an order |

### Order Items

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `GET` | `/api/order-items` | ADMIN | List all order items |
| `GET` | `/api/order-items/{id}` | Owner / ADMIN | Get order item by ID |
| `GET` | `/api/order-items/order/{orderId}` | All authenticated | Items by order |
| `POST` | `/api/order-items` | Owner / ADMIN | Add item to order |
| `PUT` | `/api/order-items/{id}` | ADMIN, CORPORATE | Update order item |
| `DELETE` | `/api/order-items/{id}` | ADMIN, CORPORATE | Delete order item |

### Categories

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `GET` | `/api/categories` | All | List all categories |
| `GET` | `/api/categories/{id}` | All | Get category by ID |
| `POST` | `/api/categories` | ADMIN | Create a category |
| `PUT` | `/api/categories/{id}` | ADMIN | Update a category |
| `DELETE` | `/api/categories/{id}` | ADMIN | Delete a category |

### Reviews

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `GET` | `/api/reviews` | All | List all reviews |
| `GET` | `/api/reviews/{id}` | All | Get review by ID |
| `POST` | `/api/reviews` | All authenticated | Create a review |
| `PUT` | `/api/reviews/{id}` | Owner / ADMIN | Update a review |
| `DELETE` | `/api/reviews/{id}` | Owner / ADMIN | Delete a review |

### Stores

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `GET` | `/api/stores` | ADMIN, CORPORATE | List stores |
| `GET` | `/api/stores/{id}` | Owner / ADMIN | Get store by ID |
| `POST` | `/api/stores` | ADMIN, CORPORATE | Create a store |
| `PUT` | `/api/stores/{id}` | Owner / ADMIN | Update a store |
| `DELETE` | `/api/stores/{id}` | ADMIN | Delete a store |

### Customer Profiles

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `GET` | `/api/customer-profiles` | ADMIN | List all profiles |
| `GET` | `/api/customer-profiles/{id}` | Owner / ADMIN | Get profile by ID |
| `POST` | `/api/customer-profiles` | All authenticated | Create profile |
| `PUT` | `/api/customer-profiles/{id}` | Owner / ADMIN | Update profile |
| `DELETE` | `/api/customer-profiles/{id}` | Owner / ADMIN | Delete profile |

### Shipments

| Method | Endpoint | Access | Description |
|---|---|---|---|
| `GET` | `/api/shipments` | Scoped | List shipments |
| `GET` | `/api/shipments/{id}` | Owner / ADMIN | Get shipment by ID |
| `POST` | `/api/shipments` | ADMIN, CORPORATE | Create shipment |
| `PUT` | `/api/shipments/{id}` | ADMIN, CORPORATE | Update shipment |
| `DELETE` | `/api/shipments/{id}` | ADMIN | Delete shipment |

### Admin Panel

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/admin/users` | List all users |
| `DELETE` | `/api/admin/users/{userId}` | Delete any user |
| `GET` | `/api/admin/stores` | List all stores |
| `DELETE` | `/api/admin/stores/{storeId}` | Delete any store |
| `GET` | `/api/admin/orders` | List all orders |
| `PUT` | `/api/admin/orders/{orderId}` | Update any order |
| `DELETE` | `/api/admin/orders/{orderId}` | Delete any order |

### AI Chatbot

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/ai-chat/ask` | Send a question to the AI agent |
| `POST` | `/api/chat/ask` | Legacy path (same functionality) |

---

## 🔐 Authentication Flow

```
1. POST /api/auth/register   →  { email, password, gender, roleType }
                              ←  { access_token, refresh_token, email, role }

2. POST /api/auth/login       →  { email, password }
                              ←  { access_token, refresh_token, email, role }

3. Use the access_token in subsequent requests:
   Authorization: Bearer <access_token>

4. POST /api/auth/refresh-token  →  { refreshToken }
                                 ←  { access_token, refresh_token, email, role }
```

### Example: Register + Login

```bash
# Register
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"secret123","gender":"Male","roleType":"INDIVIDUAL"}'

# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"secret123"}'

# Use the token
curl http://localhost:8080/api/products \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

---

## 🛡 Role-Based Access Control

The platform enforces a three-tier role hierarchy:

| Role | Capabilities |
|---|---|
| **ADMIN** | Full platform access. Manage all users, stores, orders, categories, and shipments. |
| **CORPORATE** | Manage own stores and products. View/manage orders related to their stores. |
| **INDIVIDUAL** | Browse products/categories. Create orders and reviews. View own data only. |

### Security Hardening

- **Registration Safety** — `ADMIN` role cannot be assigned through public registration
- **Mass Assignment Protection** — Role escalation is blocked in update endpoints
- **Ownership Verification** — Users can only access/modify their own resources
- **Stateless JWT** — No server-side sessions; tokens expire after 15 minutes
- **Method-Level Security** — `@PreAuthorize` annotations on admin-only controllers

---

## 🤖 AI Chatbot Module

The project includes a Python-based Multi-Agent Text2SQL chatbot located in the `ai-chatbot/` directory.

### Setup

```bash
cd ai-chatbot
pip install -r requirements.txt
```

### Configuration

Create an `.env` file in `ai-chatbot/`:

```env
GEMINI_API_KEY=your_gemini_api_key
DATABASE_URL=mysql://root:admin@localhost:3306/advanced_project
```

### Run

```bash
python main.py
```

The chatbot communicates with the Spring Boot backend via the `AICommunicationService`, which injects verified user identity (ID + role) into every AI request to prevent unauthorized data access.

---

## 📁 Project Structure

```
src/main/java/com/ecommerce/
├── ECommerceApplication.java          # Spring Boot entry point
├── config/
│   ├── AIDatabaseConfig.java          # Separate datasource for AI queries
│   └── OpenApiConfig.java             # Swagger/OpenAPI configuration
├── controller/
│   ├── AuthenticationController.java  # Register, login, token refresh
│   ├── AdminController.java           # Admin-only management endpoints
│   ├── UserController.java            # User CRUD
│   ├── ProductController.java         # Product CRUD
│   ├── OrderController.java           # Order CRUD
│   ├── OrderItemController.java       # Order item CRUD
│   ├── CategoryController.java        # Category CRUD
│   ├── ReviewController.java          # Review CRUD
│   ├── ShipmentController.java        # Shipment CRUD
│   ├── StoreController.java           # Store CRUD
│   ├── CustomerProfileController.java # Customer profile CRUD
│   ├── AIChatController.java          # AI chatbot endpoint
│   └── ChatController.java            # AI chatbot (legacy path)
├── exception/
│   ├── ResourceNotFoundException.java # 404 exception
│   └── GlobalExceptionHandler.java    # Centralized error handling
├── model/
│   ├── User.java                      # User entity (implements UserDetails)
│   ├── Product.java                   # Product entity
│   ├── Order.java                     # Order entity
│   ├── OrderItem.java                 # Order item entity
│   ├── Category.java                  # Category entity
│   ├── Review.java                    # Review entity
│   ├── Shipment.java                  # Shipment entity
│   ├── Store.java                     # Store entity
│   ├── CustomerProfile.java           # Customer profile entity
│   └── dto/
│       ├── AuthenticationRequest.java # Login request DTO
│       ├── AuthenticationResponse.java# JWT token response DTO
│       ├── RegisterRequest.java       # Registration request DTO
│       ├── RefreshTokenRequest.java   # Token refresh request DTO
│       ├── AIRequestDTO.java          # AI chatbot request
│       ├── AIPayloadDTO.java          # Internal AI payload
│       └── AIResponseDTO.java         # AI chatbot response
├── repository/                        # Spring Data JPA repositories
├── security/
│   ├── config/
│   │   ├── ApplicationConfig.java     # AuthenticationProvider, UserDetailsService
│   │   ├── SecurityConfig.java        # Filter chain, endpoint security rules
│   │   └── JwtProperties.java         # Externalized JWT config (record)
│   ├── filter/
│   │   └── JwtAuthenticationFilter.java # OncePerRequestFilter for JWT
│   ├── model/
│   │   └── Role.java                  # ADMIN, CORPORATE, INDIVIDUAL enum
│   └── service/
│       └── JwtService.java            # Token generation & validation
└── service/
    ├── AuthenticationService.java     # Register, login, refresh logic
    ├── UserService.java               # User business logic + RBAC
    ├── ProductService.java            # Product business logic
    ├── OrderService.java              # Order business logic + RBAC
    ├── OrderItemService.java          # Order item business logic
    ├── CategoryService.java           # Category business logic (admin only)
    ├── ReviewService.java             # Review business logic
    ├── ShipmentService.java           # Shipment business logic
    ├── StoreService.java              # Store business logic + RBAC
    ├── CustomerProfileService.java    # Customer profile business logic
    ├── AICommunicationService.java    # Bridge to Python AI service
    └── QueryExecutionService.java     # SQL execution with validation
```

---

## 🧪 Running Tests

```bash
# Run all tests
mvn test

# Run a specific test class
mvn test -Dtest=ChatControllerTest
```

---

## 📄 License

This project is developed as part of the **Advanced Programming (CSE 214)** course curriculum.

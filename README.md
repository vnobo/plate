# Plate Platform

<div align="center">

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen.svg)](https://github.com/vnobo/plate)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](./LICENSE)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.6-green.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-25-orange.svg)](https://openjdk.org/)
[![Angular](https://img.shields.io/badge/Angular-v20.0.0-red.svg)](https://angular.io/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17+-blue.svg)](https://www.postgresql.org/)

A comprehensive enterprise management platform built with **Spring Boot 3.5.6** and **Angular 20**, providing unified user, role, tenant, and menu management with advanced permission control, reactive architecture, and modern web technologies.

</div>

## Table of Contents

- [Features](#features)
- [Quick Start](#quick-start)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Configuration](#configuration)
- [API Reference](#api-reference)
- [Testing](#testing)
- [Deployment](#deployment)
- [Technology Stack](#technology-stack)
- [Contributing](#contributing)
- [License](#license)
- [Acknowledgements](#acknowledgements)

## ✨ Features

### 🏗️ Core Platform Features

- **Multi-tenant Architecture**: Full tenant isolation with dedicated resources and configurations
- **User Management**: Complete user lifecycle management with roles, permissions, and profile customization
- **Role-Based Access Control (RBAC)**: Granular permission system with inheritance and constraints
- **Menu Management**: Dynamic menu configuration with conditional visibility and access control
- **Audit Logging**: Comprehensive system activity tracking with search and analytics
- **OAuth2 Integration**: Support for GitHub, Google, Microsoft and other OAuth2 providers
- **Workflow Engine**: Configurable business process automation with approval chains

### 🚀 Technical Highlights

- **Reactive Architecture**: Built with Spring WebFlux for high performance and scalability
- **Real-time Updates**: WebSocket support for live data synchronization and notifications
- **SSL/HTTPS**: Built-in SSL support with certificate management for secure communications
- **Server-Side Rendering**: Angular SSR for improved SEO, performance, and initial load times
- **Progressive Web App**: PWA capabilities with offline support, caching, and native-like experience
- **Microservices Ready**: Designed for distributed deployment with service discovery
- **GraalVM Native Image**: Support for native compilation with GraalVM

### 🛡️ Security Features

- **JWT Authentication**: Stateless authentication with refresh token rotation
- **CSRF Protection**: Advanced cross-site request forgery protection mechanisms
- **Rate Limiting**: Intelligent API rate limiting and abuse prevention with IP tracking
- **Session Management**: Redis-based distributed session storage with expiration policies
- **Password Security**: Argon2id password hashing (stronger than BCrypt)
- **Two-Factor Authentication**: TOTP and email verification options
- **Security Headers**: CSP, HSTS, and other security headers configured by default
- **Audit Trail**: Comprehensive security event logging and alerting

## 🖥️ Prerequisites

### Backend Requirements

- **Java**: OpenJDK 25 or later
- **Database**: PostgreSQL 17+ with SSL support
- **Cache**: Redis 7.0+ for session storage, caching, and pub/sub
- **Build Tool**: Gradle 8.14+ with Kotlin DSL

### Frontend Requirements

- **Node.js**: 22.0+ (LTS recommended)
- **Package Manager**: npm 9.0+ or yarn 1.22+ or pnpm 8.0+
- **Browser Support**: Modern browsers (Chrome 90+, Firefox 88+, Safari 14+, Edge 90+)

### DevOps Requirements

- **Container**: Docker 24+ and Docker Compose v2
- **CI/CD**: GitHub Actions or Jenkins
- **Monitoring**: Prometheus and Grafana (optional)

## 🚀 Quick Start

### Prerequisites Setup

1. **Install Java 25**

   ```bash
   # Ubuntu/Debian
   sudo apt install openjdk-25-jdk

   # macOS
   brew install openjdk@25

   # Windows
   # Download from https://adoptium.net/
   ```

2. **Install PostgreSQL**

   ```bash
   # Ubuntu/Debian
   sudo apt install postgresql postgresql-contrib

   # macOS
   brew install postgresql

   # Start PostgreSQL
   sudo systemctl start postgresql
   ```

3. **Install Redis**

   ```bash
   # Ubuntu/Debian
   sudo apt install redis-server

   # macOS
   brew install redis

   # Start Redis
   sudo systemctl start redis
   ```

4. **Install Node.js 22+**

   ```bash
   # Using nvm (recommended)
   curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.0/install.sh | bash
   nvm install 22
   nvm use 22

   # Windows (using nvm-windows)
   # Download from https://github.com/coreybutler/nvm-windows/releases
   nvm install 22.0.0
   nvm use 22.0.0
   ```

### 📦 Backend Setup (Spring Boot)

1. **Clone and Setup**

   ```bash
   git clone https://github.com/vnobo/plate.git
   cd plate
   ```

2. **Database Configuration**

   ```bash
   # Create database
   sudo -u postgres psql
   CREATE DATABASE plate;
   CREATE USER farmer WITH PASSWORD '123456';
   GRANT ALL PRIVILEGES ON DATABASE plate TO farmer;
   \q
   ```

3. **SSL Certificate Setup (Optional)**

   ```bash
   # Generate SSL certificate for HTTPS
   keytool -genkeypair -alias plate -keyalg RSA -keysize 2048 -validity 365 \
     -keystore plate.jks -storetype JKS -storepass 123456 -keypass 123456 \
     -dname "CN=localhost, OU=PlateUnit, O=PlateOrg, L=Xi'an, ST=Shaanxi, C=CN"

   keytool -importkeystore -srckeystore plate.jks -srcstoretype JKS -srcstorepass 123456 \
     -destkeystore plate.p12 -deststoretype PKCS12 -deststorepass 123456

   # Copy to resources
   cp plate.p12 boot/platform/src/main/resources/
   ```

4. **Build and Run**

   ```bash
   # Build the application
   ./gradlew build

   # Run with default profile
   ./gradlew bootRun

   # Run with local profile
   ./gradlew bootRun --args='--spring.profiles.active=local'

   # Run tests
   ./gradlew test
   ```

5. **Access the Application**
   - **HTTP**: http://localhost:8080
   - **HTTPS**: https://localhost:8443 (with SSL)

### 🎨 Frontend Setup (Angular 20)

1. **Install Dependencies**

   ```bash
   # For main web application
   cd ui/web
   npm install

   # For alternative UI
   cd ui/ng-ui
   npm install

   # Using pnpm (faster alternative)
   npm install -g pnpm
   cd ui/web
   pnpm install
   ```

2. **Development Server**

   ```bash
   # Start development server
   npm run start

   # Start with specific configuration
   npm run start -- --configuration=development

   # Start with SSL
   npm run start -- --ssl

   # Build for production
   npm run build

   # Build for production with SSR
   npm run build:ssr

   # Run tests
   npm run test

   # Run tests with coverage
   npm run test -- --code-coverage

   # Run end-to-end tests
   npm run e2e
   ```

3. **Access the Frontend**
   - **Development**: http://localhost:4200
   - **Development (SSL)**: https://localhost:4200
   - **Production**: http://localhost:8080 (served by Spring Boot)
   - **Production (SSL)**: https://localhost:8443 (served by Spring Boot)

## 📁 Project Structure

```
plate/
├── boot/                          # Backend modules
│   ├── platform/                  # Core platform module
│   │   ├── src/main/java/com/plate/boot/
│   │   │   ├── BootApplication.java   # Main application class
│   │   │   ├── config/               # Configuration classes
│   │   │   ├── security/             # Security components
│   │   │   ├── relational/           # Business logic
│   │   │   └── commons/              # Common utilities
│   │   ├── src/main/resources/
│   │   │   ├── application.yml       # Main configuration
│   │   │   ├── application-local.yml # Local development config
│   │   │   └── db/migration/         # Flyway migrations
│   │   └── build.gradle             # Gradle build file
├── ui/                           # Frontend modules
│   ├── web/                      # Angular 20 main frontend
│   │   ├── src/
│   │   │   ├── app/              # Main application
│   │   │   │   ├── core/         # Core services and guards
│   │   │   │   ├── shared/       # Shared components and directives
│   │   │   │   ├── pages/        # Page components
│   │   │   │   └── layout/       # Layout components
│   │   │   ├── assets/           # Static assets
│   │   │   └── environments/     # Environment configs
│   │   ├── angular.json         # Angular CLI config
│   │   └── package.json         # NPM dependencies
│   └── ng-ui/                    # Alternative Angular UI
└── LICENSE                       # Apache 2.0 License
```

## 🔧 Configuration Guide

### Backend Configuration

#### Database Configuration (`application-local.yml`)

```yaml
spring:
  r2dbc:
    url: r2dbc:postgres://localhost:5432/plate
    username: farmer
    password: 123456
  redis:
    host: localhost
    port: 6379
```

#### Security Configuration

- **OAuth2**: Configured in `application.yml`
- **JWT Secret**: Auto-generated, can be overridden
- **Session Timeout**: 8 hours by default
- **CORS**: Configured for development

### Frontend Configuration

#### Environment Variables (`ui/web/src/envs/`)

```typescript
// environment.ts
export const environment = {
  production: false,
  apiUrl: "http://localhost:8080",
  socketUrl: "ws://localhost:8080/ws",
};
```

#### Build Configuration (`angular.json`)

- **Development**: Optimized for debugging
- **Production**: Optimized for performance
- **SSR**: Server-side rendering enabled

## 🐳 Docker Deployment

### Production Docker Setup

1. **Build Docker Images**

   ```bash
   # Build backend
   docker build -t plate-backend ./boot/platform

   # Build frontend
   docker build -t plate-frontend ./ui/web
   ```

2. **Docker Compose**

   ```yaml
   version: "3.8"
   services:
     postgres:
       image: postgres:17
       environment:
         POSTGRES_DB: plate
         POSTGRES_USER: farmer
         POSTGRES_PASSWORD: 123456
       ports:
         - "5432:5432"

     redis:
       image: redis:7-alpine
       ports:
         - "6379:6379"

     plate-backend:
       image: plate-backend
       ports:
         - "8080:8080"
         - "8443:8443"
       environment:
         - SPRING_PROFILES_ACTIVE=docker
       depends_on:
         - postgres
         - redis
   ```

3. **Run with Docker Compose**
   ```bash
   docker-compose up -d
   ```

## 🧪 Testing

### Backend Testing

```bash
# Run all tests
./gradlew test

# Run integration tests
./gradlew integrationTest

# Run with coverage
./gradlew jacocoTestReport
```

### Frontend Testing

```bash
# Run unit tests
npm run test

# Run e2e tests
npm run e2e

# Run tests with coverage
npm run test -- --code-coverage
```

## 🔍 API Documentation

The Plate Platform provides comprehensive API documentation through Swagger UI and OpenAPI specifications.

### API Documentation Access

- **Swagger UI**: Available at `/swagger-ui.html` when running the application
- **OpenAPI Specification**: Available at `/v3/api-docs` in JSON format
- **ReDoc**: Available at `/redoc` for a more user-friendly documentation experience

### Key API Endpoints

#### Authentication Endpoints

- `POST /oauth2/login` - User login with credentials
- `POST /oauth2/logout` - User logout and token invalidation
- `GET /oauth2/authorize` - OAuth2 authorization flow initiation
- `POST /oauth2/token` - OAuth2 token exchange
- `POST /captcha/code` - Get captcha for login verification
- `POST /auth/mfa/verify` - Verify multi-factor authentication

#### User Management

- `GET /sec/v1/users` - List users (paginated, filterable, sortable)
- `POST /sec/v1/users` - Create user with roles and permissions
- `PUT /sec/v1/users/{id}` - Update user information
- `PATCH /sec/v1/users/{id}` - Partial update of user information
- `DELETE /sec/v1/users/{id}` - Delete or deactivate user
- `GET /sec/v1/users/me` - Get current user profile

#### Group Management

- `GET /sec/v1/groups` - List groups with pagination and filtering
- `POST /sec/v1/groups` - Create new group
- `PUT /sec/v1/groups/{id}` - Update group details
- `DELETE /sec/v1/groups/{id}` - Delete group
- `GET /sec/v1/groups/{id}/members` - List group members
- `POST /sec/v1/groups/{id}/members` - Add members to group
- `DELETE /sec/v1/groups/{id}/members/{userId}` - Remove member from group

#### Menu and Permission Management

- `GET /sec/v1/menus` - Get available menus for current user
- `POST /sec/v1/menus` - Create new menu item (admin only)
- `GET /sec/v1/permissions` - List all permissions
- `POST /sec/v1/roles/{id}/permissions` - Assign permissions to role

## 🛠️ Technology Stack

### Backend Technologies

- **Spring Boot 3.5.6** - Main application framework
- **Spring Security** - Authentication and authorization
- **Spring WebFlux** - Reactive web framework
- **Spring Data R2DBC** - Reactive database access
- **PostgreSQL** - Primary database
- **Redis** - Cache and session storage
- **Flyway** - Database migration tool
- **GraalVM** - Native image compilation
- **Log4j2** - Logging framework

### Frontend Technologies

- **Angular 20** - Main frontend framework
- **Angular Material** - UI component library
- **RxJS** - Reactive programming library
- **TypeScript** - Programming language
- **Tabler Icons** - Icon library
- **Bootstrap** - CSS framework (ng-ui)

### DevOps & Tools

- **Gradle** - Build tool
- **Docker** - Containerization
- **GitHub Actions** - CI/CD
- **JUnit 5** - Testing framework
- **Karma** - Frontend test runner

## 🤝 Contributing

We welcome contributions to the Plate Platform! Here's how you can help:

### Contribution Process

1. **Fork the Repository**: Create your own fork of the project
2. **Create a Feature Branch**: `git checkout -b feature/amazing-feature`
3. **Commit Changes**: `git commit -m 'Add some amazing feature'`
4. **Push to Branch**: `git push origin feature/amazing-feature`
5. **Open a Pull Request**: Submit your changes for review

### Code Style Guidelines

- **Backend**: Follow Google Java Style Guide
- **Frontend**: Follow Angular Style Guide
- **Use meaningful variable and function names**
- **Write comprehensive comments and documentation**
- **Include appropriate tests for new features**
- **Update documentation when making changes**

### Pull Request Requirements

- Clear description of changes
- Reference to related issues
- Passing tests
- Updated documentation if needed
- Follows the project's coding standards

## 📄 License

This project is licensed under the **Apache License 2.0** - see the [LICENSE](./LICENSE) file for details.

The Apache License 2.0 is a permissive free software license that allows users to freely use, modify, and distribute the software, including for commercial purposes, under certain conditions.

## 📊 Project Status

Plate Platform is under active development. We're constantly adding new features and improvements.

**Current version**: 0.0.1 (Development)

**Roadmap**:

- [ ] Enhanced multi-tenant features
- [ ] Advanced workflow engine
- [ ] AI integration capabilities
- [ ] Mobile application
- [ ] Advanced analytics and reporting

## 🙏 Acknowledgements

- **Spring Team** for the amazing Spring Boot framework
- **Angular Team** for the powerful Angular framework
- **PostgreSQL Team** for the robust database
- **Redis Team** for the high-performance cache

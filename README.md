# Plate Platform

<div align="center">

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen.svg)](https://github.com/vnobo/plate)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](./LICENSE)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.0--M3-green.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-25-orange.svg)](https://openjdk.org/)
[![Angular](https://img.shields.io/badge/Angular-v20.0.0-red.svg)](https://angular.io/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17+-blue.svg)](https://www.postgresql.org/)
[![Redis](https://img.shields.io/badge/Redis-7.0+-red.svg)](https://redis.io/)

A modern enterprise management platform built with **Spring Boot 4.0.0-M3** and **Angular 20**, featuring reactive architecture, comprehensive user management, role-based access control, and advanced security features.

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
- **Group Management**: Hierarchical group structures with member management
- **Role-Based Access Control (RBAC)**: Granular permission system with inheritance and constraints
- **Menu Management**: Dynamic menu configuration with conditional visibility and access control
- **Audit Logging**: Comprehensive system activity tracking with search and analytics
- **OAuth2 Integration**: Support for GitHub and other OAuth2 providers
- **Captcha Verification**: Enhanced security with captcha protection for authentication

### 🚀 Technical Highlights

- **Reactive Architecture**: Built with Spring WebFlux for high performance and scalability
- **R2DBC Database Access**: Reactive database connectivity with PostgreSQL
- **Redis Integration**: Distributed caching and session management
- **HTTP/2 Support**: Modern protocol for improved performance
- **Server-Side Rendering**: Angular SSR for improved SEO and initial load times
- **Progressive Web App**: PWA capabilities with service worker support
- **GraalVM Native Image**: Support for native compilation with GraalVM
- **Flyway Migrations**: Automated database schema management

### 🛡️ Security Features

- **JWT Authentication**: Stateless authentication with secure token management
- **CSRF Protection**: Advanced cross-site request forgery protection
- **Session Management**: Redis-based distributed session storage
- **Captcha Protection**: Visual verification for login security
- **OAuth2 Client**: External authentication provider integration
- **Security Headers**: Comprehensive security headers configuration
- **Audit Trail**: Detailed security event logging and monitoring

## 🖥️ Prerequisites

### Backend Requirements

- **Java**: OpenJDK 25 or later (required for Spring Boot 4.0.0-M3)
- **Database**: PostgreSQL 17+ with SSL support
- **Cache**: Redis 7.0+ for session storage, caching, and pub/sub
- **Build Tool**: Gradle 8.14+ with Kotlin DSL
- **Note**: Spring Boot 4.0.0-M3 requires Java 21+ and includes breaking changes from Spring Boot 3.x

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

### 📦 Backend Setup (Spring Boot 4.0.0-M3)

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

   # Build native image (requires GraalVM)
   ./gradlew nativeCompile
   ```

5. **Access the Application**
   - **HTTP**: http://localhost:8080
   - **HTTPS**: https://localhost:8443 (with SSL)
   - **Note**: Spring Boot 4.0.0-M3 includes enhanced reactive capabilities and improved native image support

### 🎨 Frontend Setup (Angular 20)

1. **Install Dependencies**

   ```bash
   # Navigate to frontend directory
   cd ui/web
   
   # Install dependencies (Angular 20 compatible)
   npm install

   # Alternative package managers (optional)
   # npm install -g pnpm && pnpm install
   # npm install -g yarn && yarn install
   ```

2. **Development Server**

   ```bash
   # Start development server (Angular 20)
   npm run start

   # Build for production
   npm run build

   # Build for production with SSR
   npm run build:ssr

   # Serve SSR application
   npm run serve:ssr:web

   # Run tests
   npm run test

   # Run tests with coverage
   npm run test -- --code-coverage
   ```

3. **Access the Frontend**
   - **Development**: http://localhost:4200
   - **Production (SSR)**: http://localhost:4000 (after running `npm run serve:ssr:web`)
   - **Production**: http://localhost:8080 (served by Spring Boot)
   - **Note**: Angular 20 includes improved SSR performance and enhanced developer experience

## 📁 Project Structure

```
plate/
├── boot/                          # Backend modules
│   ├── platform/                  # Core platform module
│   │   ├── src/main/java/com/plate/boot/
│   │   │   ├── BootApplication.java   # Main application class
│   │   │   ├── config/               # Configuration classes
│   │   │   ├── security/             # Security components (OAuth2, captcha, user/group management)
│   │   │   ├── relational/           # Business logic (menus, logging)
│   │   │   └── commons/              # Common utilities (query fragments, caching, exceptions)
│   │   ├── src/main/resources/
│   │   │   ├── application.yml       # Main configuration
│   │   │   ├── application-local.yml # Local development config
│   │   │   └── db/migration/         # Flyway database migrations
│   │   └── build.gradle             # Gradle build configuration
├── ui/                           # Frontend modules
│   └── web/                      # Angular 20 frontend with SSR
│       ├── src/
│       │   ├── app/              # Main application
│       │   │   ├── core/         # Core services (HTTP interceptor, token, theme)
│       │   │   ├── pages/        # Page components (dashboard, home, passport)
│       │   │   ├── layout/       # Layout components
│       │   │   └── plugins/      # UI plugins (modals, alerts, toasts)
│       │   ├── assets/           # Static assets (images, icons)
│       │   └── envs/             # Environment configurations
│       ├── angular.json         # Angular CLI configuration
│       ├── package.json         # NPM dependencies
│       └── proxy.conf.json      # Development proxy configuration
└── LICENSE                       # Apache 2.0 License
```

## 🔧 Configuration Guide

### Backend Configuration

#### Database Configuration (`application-local.yml`)

```yaml
spring:
  r2dbc:
    url: r2dbc:postgres://127.0.0.1:5432/plate
    username: farmer
    password: 123456
    properties:
      timeZone: "GMT+8"
      applicationName: plate
  flyway:
    url: jdbc:postgresql://127.0.0.1:5432/plate
    user: farmer
    password: 123456
  data.redis.host: 127.0.0.1
```

#### Security Configuration

- **OAuth2**: GitHub OAuth2 client configuration
- **Session Management**: Redis-based with 8-hour timeout
- **Captcha**: Visual verification for authentication
- **CORS**: Configured for development environments

### Frontend Configuration

#### Environment Variables (`ui/web/src/envs/`)

```typescript
// env.dev.ts
export const env = {
  production: false,
  apiUrl: "http://localhost:8080"
};

// env.ts (production)
export const env = {
  production: true,
  apiUrl: "http://localhost:8080"
};
```

#### Development Proxy (`proxy.conf.json`)

```json
{
  "/sec": {
    "target": "http://localhost:8080",
    "secure": false
  },
  "/rel": {
    "target": "http://localhost:8080",
    "secure": false
  }
}
```

## 🐳 Docker Deployment

### Production Docker Setup

1. **Build Docker Images**

   ```bash
   # Build backend (Spring Boot 4.0.0-M3)
   docker build -t plate-backend ./boot/platform

   # Build frontend (Angular 20)
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

4. **Native Image Deployment (Optional)**
   ```bash
   # Build native image
   ./gradlew nativeCompile

   # Run native executable
   ./build/native/nativeCompile/plate-platform
   ```

## 🧪 Testing

### Backend Testing (Spring Boot 4.0.0-M3)

```bash
# Run all tests
./gradlew test

# Run integration tests
./gradlew integrationTest

# Run with coverage
./gradlew jacocoTestReport

# Run native tests (requires GraalVM)
./gradlew nativeTest
```

### Frontend Testing (Angular 20)

```bash
# Run unit tests
npm run test

# Run tests with coverage
npm run test -- --code-coverage

# Run end-to-end tests (if configured)
npm run e2e
```

## 🔍 API Documentation

The Plate Platform provides RESTful APIs with reactive endpoints. API documentation is available through the application's built-in endpoints.

### API Path Prefixes

- **Security APIs**: `/sec/*` - User, group, tenant management and authentication
- **Relational APIs**: `/rel/*` - Business data and logging operations

### Key API Endpoints

#### Authentication & Security

- `POST /oauth2/login` - User authentication
- `POST /oauth2/logout` - User logout
- `POST /captcha/code` - Generate captcha for verification
- `GET /sec/users` - List users with pagination
- `POST /sec/users` - Create new user
- `PUT /sec/users/{id}` - Update user information
- `DELETE /sec/users/{id}` - Delete user

#### Group Management

- `GET /sec/groups` - List groups
- `POST /sec/groups` - Create new group
- `PUT /sec/groups/{id}` - Update group
- `DELETE /sec/groups/{id}` - Delete group
- `GET /sec/groups/{id}/members` - List group members
- `POST /sec/groups/{id}/members` - Add member to group
- `DELETE /sec/groups/{id}/members/{userId}` - Remove member from group

#### Tenant Management

- `GET /sec/tenants` - List tenants
- `POST /sec/tenants` - Create tenant
- `PUT /sec/tenants/{id}` - Update tenant
- `GET /sec/tenants/{id}/members` - List tenant members

#### Menu Management

- `GET /rel/menus` - Get available menus
- `POST /rel/menus` - Create menu item
- `PUT /rel/menus/{id}` - Update menu
- `DELETE /rel/menus/{id}` - Delete menu

#### Audit Logging

- `GET /rel/loggers` - View system logs
- `POST /rel/loggers` - Create log entry

## 🛠️ Technology Stack

### Backend Technologies

- **Spring Boot 4.0.0-M3** - Main application framework (latest milestone release)
- **Spring Security** - Authentication and authorization
- **Spring WebFlux** - Reactive web framework
- **Spring Data R2DBC** - Reactive database access
- **Spring Session** - Distributed session management
- **PostgreSQL** - Primary relational database
- **Redis** - Cache and session storage
- **Flyway 11.13.2** - Database migration tool
- **GraalVM Native Image** - Native compilation support
- **Log4j2** - Logging framework
- **R2DBC PostgreSQL 1.1.0** - Reactive database driver

### Frontend Technologies

- **Angular 20** - Main frontend framework
- **Angular Material** - UI component library
- **Angular SSR** - Server-side rendering
- **RxJS 7.8.0** - Reactive programming library
- **TypeScript 5.8.2** - Programming language
- **Tabler Icons** - Icon library
- **Tabler Core** - UI component framework
- **Express.js 5.1.0** - Server for SSR

### DevOps & Tools

- **Gradle** - Build tool with Kotlin DSL
- **Docker** - Containerization
- **GitHub Actions** - CI/CD pipeline
- **JUnit 5** - Testing framework
- **TestContainers** - Integration testing
- **Karma** - Frontend test runner
- **Jasmine** - Frontend testing framework

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

## 🚀 Migration Notes

### Spring Boot 4.0.0-M3 Upgrade

This project has been upgraded to Spring Boot 4.0.0-M3, which includes several important changes:

- **Java 25 Requirement**: Spring Boot 4.x requires Java 21 or later
- **Enhanced Reactive Support**: Improved WebFlux and R2DBC integration
- **Native Image Improvements**: Better GraalVM native compilation support
- **Breaking Changes**: Some configuration properties and APIs may have changed from Spring Boot 3.x

### Angular 20 Upgrade

The frontend has been upgraded to Angular 20, featuring:

- **Improved SSR Performance**: Enhanced server-side rendering capabilities
- **Modern Development Experience**: Updated tooling and build optimizations
- **Enhanced TypeScript Support**: Better type checking and developer experience

## 📊 Project Status

Plate Platform is under active development with the latest technology stack and core features implemented.

**Current version**: 0.0.1 (Development)

**Technology Stack**:
- ✅ **Backend**: Spring Boot 4.0.0-M3 with Java 25
- ✅ **Frontend**: Angular 20 with SSR support
- ✅ **Database**: PostgreSQL 17+ with R2DBC reactive driver
- ✅ **Cache**: Redis 7.0+ for distributed sessions

**Implemented Features**:
- ✅ Multi-tenant architecture with user/group management
- ✅ Reactive backend with Spring WebFlux and R2DBC
- ✅ Angular 20 frontend with SSR support
- ✅ OAuth2 authentication with GitHub integration
- ✅ Role-based access control (RBAC)
- ✅ Menu and permission management
- ✅ Audit logging system
- ✅ Captcha verification for security

**Roadmap**:
- [ ] Enhanced workflow engine
- [ ] Advanced reporting and analytics
- [ ] Mobile application support
- [ ] Additional OAuth2 providers
- [ ] Performance optimizations

## 🙏 Acknowledgements

- **Spring Team** for the comprehensive Spring Boot framework and reactive stack
- **Angular Team** for the powerful Angular framework and SSR capabilities
- **PostgreSQL Team** for the robust and scalable database
- **Redis Team** for the high-performance caching and session storage
- **Tabler Team** for the beautiful UI components and icons
- **Flyway Team** for the reliable database migration tool

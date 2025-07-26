# Plate Platform

<div align="center">

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen.svg)](https://github.com/vnobo/plate)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](./LICENSE)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.3-green.svg)](https://spring.io/projects/spring-boot)
[![Angular](https://img.shields.io/badge/Angular-v20.0.0-red.svg)](https://angular.io/)

A comprehensive system management platform built with **Spring Boot 3** and **Angular 20**, providing unified user, role, tenant, and menu management with advanced permission control and AI integration.

</div>

## ✨ Features

### 🏗️ Core Platform Features
- **Multi-tenant Architecture**: Full tenant isolation and management
- **User Management**: Complete user lifecycle management with roles and permissions
- **Role-Based Access Control (RBAC)**: Granular permission system
- **Menu Management**: Dynamic menu configuration and access control
- **Audit Logging**: Comprehensive system activity tracking
- **OAuth2 Integration**: Support for GitHub and other OAuth2 providers

### 🚀 Technical Highlights
- **Reactive Architecture**: Built with Spring WebFlux for high performance
- **Real-time Updates**: WebSocket support for live data synchronization
- **SSL/HTTPS**: Built-in SSL support for secure communications
- **Server-Side Rendering**: Angular SSR for improved SEO and performance
- **Progressive Web App**: PWA capabilities with service workers

### 🛡️ Security Features
- **JWT Authentication**: Stateless authentication tokens
- **CSRF Protection**: Cross-site request forgery protection
- **Rate Limiting**: API rate limiting and abuse prevention
- **Session Management**: Redis-based distributed session storage
- **Password Security**: BCrypt password encryption

## 🖥️ Environment Requirements

### Backend Requirements
- **Java**: OpenJDK 21 or later
- **Database**: PostgreSQL 17+ with SSL support
- **Cache**: Redis 7.0+ for session storage and caching
- **Build Tool**: Gradle 8.14+

### Frontend Requirements
- **Node.js**: 22.0+ (LTS recommended)
- **Package Manager**: npm 9.0+ or yarn 1.22+
- **Browser Support**: Modern browsers (Chrome 90+, Firefox 88+, Safari 14+)

## 🚀 Quick Start

### Prerequisites

1. **Install Java 21**
   ```bash
   # Ubuntu/Debian
   sudo apt install openjdk-21-jdk
   
   # macOS
   brew install openjdk@21
   
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

4. **Install Node.js 20+**
   ```bash
   # Using nvm (recommended)
   curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.0/install.sh | bash
   nvm install 20
   nvm use 20
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
   # For PC web application
   cd ui/web
   npm install
   
   # For alternative UI
   cd ui/ng-ui
   npm install
   ```

2. **Development Server**
   ```bash
   # Start development server
   npm run start
   
   # Start with specific configuration
   npm run start -- --configuration=development
   
   # Build for production
   npm run build
   
   # Run tests
   npm run test
   ```

3. **Access the Frontend**
   - **Development**: http://localhost:4200
   - **Production**: http://localhost:8080 (served by Spring Boot)

## 📁 Project Structure

```
plate/
├── boot/platform/                 # Spring Boot backend
│   ├── src/main/java/com/plate/boot/
│   │   ├── BootApplication.java   # Main application class
│   │   ├── config/               # Configuration classes
│   │   ├── security/             # Security components
│   │   ├── relational/           # Business logic
│   │   └── commons/              # Common utilities
│   ├── src/main/resources/
│   │   ├── application.yml       # Main configuration
│   │   ├── application-local.yml # Local development config
│   │   └── db/migration/         # Flyway migrations
│   └── build.gradle             # Gradle build file
├── ui/web/                       # Angular 20 frontend
│   ├── src/
│   │   ├── app/                  # Main application
│   │   ├── core/                 # Core services
│   │   ├── pages/                # Feature modules
│   │   └── environments/         # Environment configs
│   ├── angular.json             # Angular CLI config
│   └── package.json             # NPM dependencies
└── ui/ng-ui/                     # Alternative Angular UI
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
  ai:
    openai:
      api-key: your-openai-key
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
  apiUrl: 'http://localhost:8080',
  socketUrl: 'ws://localhost:8080/ws'
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
   version: '3.8'
   services:
     postgres:
       image: postgres:15
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

### Authentication Endpoints
- `POST /oauth2/login` - User login
- `POST /oauth2/logout` - User logout
- `GET /oauth2/authorize` - OAuth2 authorization
- `POST /captcha/code` - Get captcha

### User Management
- `GET /sec/v1/users` - List users (paginated)
- `POST /sec/v1/users` - Create user
- `PUT /sec/v1/users/{id}` - Update user
- `DELETE /sec/v1/users/{id}` - Delete user

### Group Management
- `GET /sec/v1/groups` - List groups
- `POST /sec/v1/groups` - Create group
- `PUT /sec/v1/groups/{id}` - Update group
- `GET /sec/v1/groups/{id}/members` - List group members

## 🐛 Troubleshooting

### Common Issues

#### Database Connection Issues
```bash
# Check PostgreSQL status
sudo systemctl status postgresql

# Check connection
psql -U farmer -d plate -h localhost

# Reset database
dropdb plate
createdb plate
```

#### Port Already in Use
```bash
# Find process using port 8080
lsof -i :8080

# Kill process
kill -9 <PID>
```

#### Frontend Build Issues
```bash
# Clear npm cache
npm cache clean --force

# Delete node_modules and reinstall
rm -rf node_modules package-lock.json
npm install

# Clear Angular cache
npm run clean
```

#### SSL Certificate Issues
```bash
# Regenerate certificate
keytool -delete -alias plate -keystore plate.jks -storepass 123456
# Then follow SSL setup steps again
```

### Debug Mode
```bash
# Backend debug
./gradlew bootRun --debug-jvm

# Frontend debug
npm run start -- --configuration=development --verbose
```

## 🤝 Contributing

1. **Fork the Repository**
2. **Create Feature Branch**
   ```bash
   git checkout -b feature/amazing-feature
   ```
3. **Commit Changes**
   ```bash
   git commit -m 'Add amazing feature'
   ```
4. **Push to Branch**
   ```bash
   git push origin feature/amazing-feature
   ```
5. **Open Pull Request**

### Code Style
- **Backend**: Follow Spring Boot conventions
- **Frontend**: Follow Angular style guide
- **Commit**: Use conventional commits
- **PR**: Include tests and documentation

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- **Spring Team** for the amazing Spring Boot framework
- **Angular Team** for the powerful Angular framework
- **ng-zorro Team** for the beautiful UI components
- **PostgreSQL Team** for the robust database
- **Redis Team** for the high-performance cache

## 📞 Support

- **Issues**: [GitHub Issues](https://github.com/vnobo/plate/issues)
- **Discussions**: [GitHub Discussions](https://github.com/vnobo/plate/discussions)
- **Documentation**: [Wiki](https://github.com/vnobo/plate/wiki)

---

<div align="center">
  <b>⭐ Star this repository if you find it helpful!</b>
</div>
README.md

# Plate Platform

Plate Platform is a modern, Spring Boot 4.0.0-M3-based full-stack web application platform designed with reactive
programming model and microservices architecture. The platform provides complete user management, security
authentication, logging, menu management, multi-tenancy support, and other functionalities.

## Project Structure

This project is a Gradle multi-module Spring Boot application with the main application entry located in the
`com.plate.boot` package. It contains various sub-packages handling different aspects such as security, relational data
processing, and common utilities. The project follows a modular architecture with a parent "plate" project and a "
platform" module.

## Features

The application includes the following key features:

- **User Management**: Complete user-related operations and authentication functionalities
- **Security Authentication**: Robust security configuration based on OAuth2 with GitHub OAuth2 login support
- **Logging**: Comprehensive logging functionality with pagination and cleanup features
- **Menu Management**: Dynamic menu items and their related permissions
- **Multi-tenancy Support**: Built-in tenant support suitable for SaaS applications
- **Caching Mechanism**: Performance enhancement through Redis caching
- **Reactive Programming**: Reactive, non-blocking operations built on Spring WebFlux
- **Dynamic Query Building**: Custom utility classes (QueryHelper, QueryJsonHelper, QueryFragment) for secure SQL
  construction
- **Full-text Search**: Support for Chinese full-text search functionality
- **JSON Data Processing**: PostgreSQL's JSON/JSONB field support
- **Concurrency Control**: Support for virtual threads (Java 25)
- **CSRF Protection**: Anti-cross-site request forgery protection
- **Session Management**: Limiting to one concurrent session per user

## Technology Stack

### Core Technologies

- **Spring Boot**: 4.0-M3
- **Spring WebFlux**: Reactive web framework
- **Spring Data R2DBC**: Reactive database access
- **Spring Security**: Security framework
- **Spring Data Redis**: Reactive Redis data access
- **Spring Session**: Redis-based session management
- **Spring OAuth2**: OAuth2 client support

### Database Technologies

- **PostgreSQL**: Primary database
- **R2DBC PostgreSQL**: Reactive PostgreSQL driver
- **Flyway**: Database migration tool

### Other Technologies

- **Redis**: Caching and session storage
- **Java**: Version 25 with virtual threads enabled
- **Gradle**: Build tool
- **Lombok**: Code simplification
- **Log4j2**: Logging framework
- **Jackson**: JSON processing
- **UUID Creator**: Time-ordered UUID generation

## Dependencies List

### Core Dependencies

- Spring Boot Starter Actuator
- Spring Boot Starter Jackson
- Spring Boot Starter Log4j2
- Spring Boot Starter AspectJ
- Spring Boot Starter Cache
- Spring Boot Starter Security
- Spring Boot Starter OAuth2 Client
- Spring Boot Starter Validation
- Spring Boot Starter WebFlux
- Spring Boot Starter Data Redis Reactive
- Spring Boot Starter Session Data Redis
- Spring Boot Starter Data R2DBC
- Spring Boot Starter Flyway
- Spring JDBC

### Database Related

- PostgreSQL R2DBC Driver
- PostgreSQL JDBC Driver
- Flyway PostgreSQL Database Plugin

### Utility Libraries

- Google Guava: 33+
- UUID Creator: 6+
- Lombok

### Test Dependencies

- Spring Boot Starter Test
- Reactor Test
- Spring Security Test
- Spring Boot Testcontainers
- Testcontainers JUnit Jupiter
- Testcontainers R2DBC
- Testcontainers PostgreSQL
- Redis Testcontainers

## Environment Requirements

### Development Environment

- **Java**: 25 or higher
- **Gradle**: 8.0 or higher
- **PostgreSQL**: 14 or higher (with UUIDv7 and Chinese full-text search support)
- **Redis**: 6.0 or higher

### Runtime Environment

- **Operating System**: Any system supporting Java 25
- **Memory**: At least 4GB RAM recommended
- **Storage**: At least 1GB available space

## Installation Instructions

### Prerequisites

1. Install Java 25
2. Install and run PostgreSQL database
3. Install and run Redis server
4. Install Gradle

### Setup Steps

1. Clone the repository
   ```bash
   git clone <repository-url>
   cd plate-platform
   ```

2. Set up PostgreSQL database (ensure UUID and Chinese full-text search extensions are enabled)
   ```sql
   create extension if not exists "uuid-ossp";
   create extension if not exists pg_trgm;
   create extension if not exists zhparser;
   ```

3. Ensure Redis server is running

4. Update local database and Redis configurations in `platform/src/main/resources/application-local.yml` as needed

5. Build the project
   ```bash
   ./gradlew build
   ```

## Usage

### Running the Application

To run the application, execute the following command from the root directory:

```bash
./gradlew :platform:bootRun
```

For local development, use the local configuration file:

```bash
./gradlew :platform:bootRun --args='--spring.profiles.active=local'
```

The application will be accessible at `http://localhost:8080`.

### Docker Execution

Build Docker image:

```bash
./gradlew bootBuildImage
```

Run Docker container:

```bash
docker run -p 8080:8080 localhost:5000/plate-platform:latest
```

## Configuration Options

### Server Configuration

- `server.port`: Server port (default 8080)
- `server.http2.enabled`: Enable HTTP/2 (default true)
- `server.shutdown`: Shutdown mode (default graceful)
- `server.compression.enabled`: Enable compression (default true)

### Spring Configuration

- `spring.threads.virtual.enabled`: Enable virtual threads (default true)
- `spring.main.keep-alive`: Keep application alive (default true)
- `spring.application.name`: Application name (default plate)
- `spring.application.group`: Application group (default platform)

### WebFlux Configuration

- `spring.webflux.format.time`: Time format
- `spring.webflux.format.date-time`: Date-time format
- `spring.webflux.format.date`: Date format
- `spring.webflux.properties.path-prefixes`: Path prefix configuration

### Jackson Configuration

- `spring.jackson.date-format`: Date format
- `spring.jackson.time-zone`: Time zone (default GMT+8)
- `spring.jackson.locale`: Localization settings (default zh_CN)

### HTTP Codec Configuration

- `spring.http.codecs.max-in-memory-size`: Maximum memory size (default 256KB)
- `spring.http.codecs.log-request-details`: Log request details (default false)

### Cache Configuration

- `spring.cache.type`: Cache type (default redis)
- `spring.cache.redis.key-prefix`: Redis key prefix (default "plate:caches:")
- `spring.cache.redis.time-to-live`: Expiration time (default 10 minutes)
- `spring.cache.redis.use-key-prefix`: Use key prefix (default true)

### Session Configuration

- `spring.session.timeout`: Session timeout (default 8 hours)
- `spring.session.redis.cleanup-cron`: Cleanup Cron expression (default every 5 seconds)

### R2DBC Connection Pool Configuration

- `spring.r2dbc.pool.max-size`: Maximum connections (default 64)
- `spring.r2dbc.pool.max-idle-time`: Maximum idle time (default 10 minutes)
- `spring.r2dbc.pool.max-acquire-time`: Maximum acquire time (default 30 seconds)
- `spring.r2dbc.pool.acquire-retry`: Acquire retry count (default 3)
- `spring.r2dbc.pool.validation-query`: Validation query (default SELECT 1)
- `spring.r2dbc.pool.max-validation-time`: Maximum validation time (default 2 seconds)
- `spring.r2dbc.pool.max-create-connection-time`: Maximum connection creation time (default 1 second)

### Redis Configuration

- `spring.data.redis.timeout`: Timeout (default 30 seconds)
- `spring.data.redis.connect-timeout`: Connection timeout (default 10 seconds)
- `spring.data.redis.repositories.enabled`: Enable repositories (default false)

### Flyway Configuration

- `spring.flyway.baseline-on-migrate`: Establish baseline on migration (default true)
- `spring.flyway.baseline-version`: Baseline version (default 1.0.0)
- `spring.flyway.baseline-description`: Baseline description

### OAuth2 Configuration

- `spring.security.oauth2.client.registration.github.client-id`: GitHub client ID

## API Documentation

API documentation can be accessed through Swagger UI. After starting the application, you can access the following
address:

`http://localhost:8080/swagger-ui.html`

### Main API Endpoints

#### Security-related Endpoints

- `/oauth2/login` - Get login token
- `/oauth2/csrf` - Get CSRF token
- `/oauth2/bind` - Bind OAuth2 client
- `/oauth2/change/password` - Change password
- `/oauth2/logout` - Logout

#### Logger-related Endpoints

- `/loggers/page` - Paginated log records

#### Menu-related Endpoints

- `/rel/menus` - Menu management operations

#### Security-related Endpoints

- `/sec/users` - User management
- `/sec/groups` - User group management
- `/sec/tenants` - Tenant management
- `/sec/authorities` - Authority management

## Sample Code

### Using QueryHelper for Queries

```java
// Create query fragment
QueryFragment queryFragment = QueryFragment.from("users")
                .column("id", "name", "email")
                .where("age > :age")
                .orderBy("name ASC");

// Bind parameters
queryFragment.

put("age",18);

// Generate SQL query
String sqlQuery = queryFragment.querySql();
```

### Using QueryHelper to Build Queries

```java
// Create user request object
UserRequest userRequest = new UserRequest();
userRequest.

setUsername("john");

// Create pagination object
Pageable pageable = PageRequest.of(0, 10);

// Build query fragment
QueryFragment queryFragment = QueryHelper.query(userRequest, pageable);
String sqlQuery = queryFragment.querySql();
```

### Using QueryJsonHelper for JSON Queries

```java
// Create JSON parameter map
Map<String, Object> jsonParams = new HashMap<>();
jsonParams.

put("extend.requestBody.nameEq","Test User");
jsonParams.

put("extend.emailEq","testuser@example.com");

// Build JSON query conditions
QueryFragment.Condition condition = QueryJsonHelper.queryJson(jsonParams, "a");
QueryFragment queryFragment = QueryFragment.conditional(condition);
String sqlQuery = queryFragment.querySql();
```

### Full-text Search Example

```java
// Use full-text search
QueryFragment queryFragment = QueryFragment.from("users")
                .column("id", "name", "email")
                .ts("bio", "test user"); // Add full-text search condition

String sqlQuery = queryFragment.querySql();
```

## Security Features

### Authentication and Authorization

- OAuth2 client support (including GitHub login)
- Role-based access control (RBAC)
- Support for administrator role (ROLE_SYSTEM_ADMINISTRATORS)
- Password encoding using DelegatingPasswordEncoder (default bcrypt)

### Session Management

- Limit one concurrent session per user
- Session timeout settings (default 8 hours)
- Redis-supported distributed sessions

### CSRF Protection

- Cookie-based CSRF token storage
- Specific endpoint exclusion from CSRF protection (such as OAuth2-related endpoints)

### Password Security

- Support for multiple password encoding algorithms (bcrypt, argon2, pbkdf2, scrypt, etc.)
- Password strength validation
- Prevention of same new and old password changes

## Database Design

### Core Table Structure

#### User Table (se_users)

- Storage of user basic information
- Multi-tenancy support
- Secure password storage
- JSON extension fields
- Full-text search vector

#### Authority Table (se_authorities)

- User authority associations
- Multi-tenancy support
- JSON extension fields

#### User Group Table (se_groups)

- User group management
- Support for hierarchical structure
- JSON extension fields
- Full-text search vector

#### Group Authority Table (se_group_authorities)

- User group authority associations
- JSON extension fields

#### Group Member Table (se_group_members)

- User group member relationships
- JSON extension fields

#### Tenant Table (se_tenants)

- Tenant information management
- Support for hierarchical structure
- JSON extension fields
- Full-text search vector

#### Tenant Member Table (se_tenant_members)

- Tenant user relationships
- JSON extension fields

#### Menu Table (se_menus)

- Menu authority management
- Support for hierarchical structure
- Multi-tenancy support
- JSON extension fields
- Full-text search vector

#### Logger Table (se_loggers)

- Operation log recording
- Multi-tenancy support
- JSON context fields
- Full-text search vector

### Database Features

- Use of UUIDv7 as primary key
- Automatic timestamp update triggers
- Chinese full-text search support
- JSON/JSONB field support
- GIN index for query optimization

## Development Guide

### Code Conventions

1. Use ContextUtils.OBJECT_MAPPER for JSON operations instead of creating new ObjectMapper instances
2. Use ContextUtils.nextId() to generate time-ordered UUIDs
3. Use ContextUtils.securityDetails() to access security context in reactive code
4. Use ContextUtils.getClientIpAddress() to extract request IP address (considering proxy headers)
5. Use @RequiredArgsConstructor for dependency injection (final fields)
6. Use @Log4j2 annotation instead of manually creating logger
7. Use the PasswordEncoder provided by ContextUtils.createDelegatingPasswordEncoder()
8. Repository paths use "rel" prefix for relational endpoints and "sec" prefix for security endpoints

### Reactive Programming

- Proper use of Mono/Flux types
- Avoid blocking operations
- Use virtual threads to improve concurrent performance

### Security Practices

- Use prepared statements to prevent SQL injection
- Properly handle CSRF tokens
- Validate user input
- Use secure password encoding

### Data Access

- Use QueryHelper, QueryJsonHelper, and QueryFragment for secure query building
- Leverage PostgreSQL's JSON capabilities
- Use full-text search to optimize query performance

## Contribution Guide

Contributions are welcome! If you encounter any issues or want to add new features, feel free to submit issues or pull
requests.

### Development Process

1. Fork the repository
2. Create a feature branch
3. Commit changes
4. Ensure all tests pass
5. Submit a pull request

### Code Style

- Follow existing code style and conventions
- Write tests for new features
- Ensure all tests pass before submitting a pull request
- Update documentation as needed
- Use the provided query building tools (QueryHelper, QueryJsonHelper, QueryFragment) for database operations to prevent
  SQL injection

### Testing Requirements

- Add unit tests for new features
- Run integration tests to ensure compatibility
- Verify security features work correctly

## Build and Test

### Building the Project

Build the entire project:

```bash
./gradlew build
```

Build only the platform module:

```bash
./gradlew :platform:build
```

### Running Tests

Run all tests:

```bash
./gradlew test
```

Run platform module tests:

```bash
./gradlew :platform:test
```

Run specific test class:

```bash
./gradlew :platform:test --tests "TestClassName"
```

## SSL Certificate Generation

> Reference: https://github.com/FiloSottile/mkcert.git

Generate SSL certificates for local development:

```bash
mkcert -cert-file localhost+2.pem -key-file localhost+2-key.pem -pkcs12 plate

keytool -importkeystore -srckeystore plate.p12 -srcstoretype pkcs12 -srcalias 1 -destkeystore plate.jks -deststoretype jks -deststorepass 123456 -destalias plate
```

Uncomment SSL configuration in `application-local.yml` to enable SSL.

## Deployment Guide

### Production Environment Deployment

1. Configure production database connection
2. Configure production Redis connection
3. Set environment variables (database passwords, Redis passwords, etc.)
4. Build production image or JAR file
5. Start the application

### Environment Variable Configuration

- `port`: Server port (default 8080)
- `github.client-id`: GitHub OAuth2 client ID
- Database connection parameters
- Redis connection parameters
- Security-related keys and passwords

## Performance Optimization

### Virtual Threads

- Enabled virtual thread support in Java 25
- Improved concurrent performance for I/O-intensive operations

### Caching Strategy

- Redis cache support
- 10-minute default TTL
- Prefix key strategy

### Database Optimization

- R2DBC connection pool configuration
- 64 maximum connections
- Automatic connection validation

## Monitoring and Operations

### Actuator Endpoints

- Health checks
- Metrics monitoring
- Environment information
- Configuration properties

### Log Management

- Log4j2 logging framework
- Structured log output
- Hierarchical log recording

## Frequently Asked Questions

### Q: How to configure database connection?

A: Configure database connection parameters in application.yml or environment variables, including URL, username, and
password.

### Q: How to add a new OAuth2 provider?

A: Add the corresponding client registration configuration in application.yml.

### Q: How to customize user permissions?

A: Use user group and permission management features, configure through se_groups, se_group_authorities, and
se_authorities tables.

### Q: How to extend user information?

A: Use JSON extension fields (extend) to store custom user data.

### Q: How to implement multi-tenant data isolation?

A: The system has built-in multi-tenancy support. All data tables contain the tenant_code field, which is automatically
filtered through the security context.

## Contact

Project author: Alex Bob
GitHub: https://github.com/vnobo

## Project Status

This project is under active development. The current version provides complete user management, security
authentication, logging, and multi-tenancy support features. We continuously improve and add new features to meet
enterprise-level application requirements.

## License Information

This project is licensed under the MIT License.

## Version History

### V1.0

- Baseline version
- Basic user management functionality
- Security authentication system
- Multi-tenancy support

### V1.0.1

- Extended functionality
- Performance optimization

### V1.0.2

- Database schema definition
- Complete table structure design
- Full-text search support

### V1.0.3

- Data initialization
- Basic data configuration

### V1.0.4

- Test data initialization
- Complete functionality verification
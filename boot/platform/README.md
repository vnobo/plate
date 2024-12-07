## Project Structure

The project is structured in a typical Spring Boot fashion, with the main application entry point in the
`com.plate.boot` package. It includes various sub-packages for different concerns such as security, relational data
handling, and common utilities.

## Features

The application includes the following key features:

- **User Management**: Handles user-related operations and authentication.
- **Security**: Implements security configurations and OAuth2 support.
- **Logging**: Manages application logs efficiently with pagination and cleanup.
- **Menus**: Manages menu items and their associated permissions.
- **Tenant Support**: Provides multi-tenancy capabilities.
- **Caching**: Utilizes caching mechanisms to improve performance.

## Dependencies

The project relies on several dependencies, including:

- Spring Boot 3.x.x
- Spring Data R2DBC
- Spring Security
- Redis
- PostgreSQL

## Getting Started

To run the application, you need to have Java 17 and Maven installed. Then, you can start the application by running the
following command from the root directory of the project:

This will start the Spring Boot application, and it should be accessible at `http://localhost:8080`.

## API Documentation

The API documentation is available using Swagger UI. Once the application is running, you can access the Swagger UI at:

## Contributing

Contributions are welcome! If you find any issues or want to add new features, feel free to open an issue or submit a
pull request.

## keystore

```bash
    mkcert -cert-file localhost+2.pem -key-file localhost+2-key.pem -pkcs12 plate
    
    keytool -importkeystore -srckeystore plate.p12 -srcstoretype pkcs12 -srcalias 1 -destkeystore plate.jks -deststoretype jks -deststorepass 123456 -destalias plate
```



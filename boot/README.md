## Project Structure

The project is organized in a typical Spring Boot manner, with the main application entry point located in the
`com.plate.boot` package. It contains various sub-packages dealing with different aspects such as security, relational
data handling, and common utilities.

## Features

The application encompasses the following key features:

- **User Management**: Deals with user-related operations and authentication.
- **Security**: Implements security configurations and provides OAuth2 support.
- **Logging**: Effectively manages application logs with pagination and cleanup functions.
- **Menus**: Manages menu items and their associated permissions.
- **Tenant Support**: Offers multi-tenancy capabilities.
- **Caching**: Utilizes caching mechanisms to enhance performance.

## Dependencies

The project depends on several dependencies, including:

- Spring Boot 3.5.x
- Spring Data R2DBC
- Spring Security
- Redis
- PostgreSQL

## Getting Started

To run the application, you need to have Java 17 and Maven installed. Then, you can start the application by running the
following command from the root directory of the project:

This will launch the Spring Boot application, and it should be accessible at `http://localhost:8080`.

## API Documentation

The API documentation can be accessed via Swagger UI. Once the application is running, you can visit the Swagger UI at:

## Contributing

Contributions are welcome! If you come across any issues or wish to add new features, feel free to open an issue or
submit a pull request.

## SSL Certificate Generation

> https://github.com/FiloSottile/mkcert.git

```bash
    mkcert -cert-file localhost+2.pem -key-file localhost+2-key.pem -pkcs12 plate
    
    keytool -importkeystore -srckeystore plate.p12 -srcstoretype pkcs12 -srcalias 1 -destkeystore plate.jks -deststoretype jks -deststorepass 123456 -destalias plate
```

### Developer Documentation: Using QueryHelper, QueryJsonHelper and QueryFragment

#### 1. Overview

`QueryHelper`, `QueryJsonHelper` and `QueryFragment` are three utility classes that work together to assist developers
in constructing and executing dynamic SQL queries, especially those involving JSON fields. These utility classes offer a
secure and flexible way to handle database queries, reducing the risk of SQL injection and simplifying the query
construction process.

#### 2. QueryFragment

`QueryFragment` is a core class used for building different parts of an SQL query, including selected columns, query
conditions, sorting and pagination.

**Usage Example**:

```java
QueryFragment queryFragment = QueryFragment.withNew()
        .addColumn("id", "name", "email")
        .addQuery("users")
        .addWhere("age > :age")
        .addOrder("name ASC");

queryFragment.

put("age",18); // Bind parameters

String sqlQuery = queryFragment.querySql(); // Generate the SQL query string
```

#### 3. QueryHelper

`QueryHelper` provides static methods to build a `QueryFragment` from an object, especially when the object contains
pagination information.

**Usage Example**:

```java
UserRequest userRequest = new UserRequest();
userRequest.

setUsername("john");

Pageable pageable = PageRequest.of(0, 10);

QueryFragment queryFragment = QueryHelper.query(userRequest, pageable);
String sqlQuery = queryFragment.querySql();
```

#### 4. QueryJsonHelper

`QueryJsonHelper` focuses on handling queries for JSON fields, allowing developers to construct SQL query conditions for
JSON data.

**Usage Example**:

```java
Map<String, Object> jsonParams = new HashMap<>();
jsonParams.

put("extend.requestBody.nameEq","Test User");
jsonParams.

put("extend.emailEq","testuser@example.com");

QueryFragment queryFragment = QueryJsonHelper.queryJson(jsonParams, "a");
String sqlQuery = queryFragment.querySql();
```

#### 5. Full-text Search Use Case

For full-text search, you can use the `addQuery` method of `QueryFragment` to add conditions for full-text search.

**Usage Example**:

```java
QueryFragment queryFragment = QueryFragment.withNew()
        .addColumn("id", "name", "email")
        .addQuery("users")
        .addWhere("to_tsvector('english', bio) @@ to_tsquery('english', :search)")
        .addOrder("ts_rank(to_tsvector('english', bio), to_tsquery('english', :search)) DESC");

queryFragment.

put("search","test user"); // Bind the full-text search parameter

String sqlQuery = queryFragment.querySql(); // Generate the SQL query string containing the full-text search
```

In this example, `to_tsvector` and `to_tsquery` are PostgreSQL's full-text search functions used to convert text into
vectors and query strings respectively.

#### 6. Integration with UserRequest

The `UserRequest` class extends the `User` class and adds additional attributes and methods for handling user requests.

**Usage Example**:

```java
UserRequest userRequest = new UserRequest();
userRequest.

setUsername("john");
userRequest.

setSecurityCode("secure-code");

// Convert UserRequest to QueryFragment
QueryFragment queryFragment = userRequest.toParamSql();
String sqlQuery = queryFragment.querySql();
```

In this example, the `toParamSql` method converts the `UserRequest` object into a `QueryFragment` instance for
constructing an SQL query.

#### 7. Precautions

- Ensure that the database connection and configuration are correctly set when using these utility classes.
- For full-text search, make sure that the database supports the full-text search function and that the corresponding
  indexes have been created.
- When binding parameters, ensure that the parameter names and values match the placeholders in the query.

Through these utility classes, developers can build and execute SQL queries more conveniently while maintaining the
security and maintainability of the code. 
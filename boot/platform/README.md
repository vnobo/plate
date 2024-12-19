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

## SSL Certificate Generation

> https://github.com/FiloSottile/mkcert.git

```bash
    mkcert -cert-file localhost+2.pem -key-file localhost+2-key.pem -pkcs12 plate
    
    keytool -importkeystore -srckeystore plate.p12 -srcstoretype pkcs12 -srcalias 1 -destkeystore plate.jks -deststoretype jks -deststorepass 123456 -destalias plate
```

### 开发者文档：使用 QueryHelper、QueryJsonHelper 和 QueryFragment

#### 1. 概述

`QueryHelper`、`QueryJsonHelper` 和 `QueryFragment` 是三个工具类，它们共同协作以帮助开发者构建和执行动态 SQL 查询，特别是涉及
JSON 字段的查询。这些工具类提供了一种安全、灵活的方式来处理数据库查询，减少了 SQL 注入的风险，并简化了查询构建过程。

#### 2. QueryFragment

`QueryFragment` 是一个核心类，用于构建 SQL 查询的各个部分，包括选择的列、查询条件、排序和分页。

**使用案例：**

```java
QueryFragment queryFragment = QueryFragment.withNew()
        .addColumn("id", "name", "email")
        .addQuery("users")
        .addWhere("age > :age")
        .addOrder("name ASC");

queryFragment.

put("age",18); // 绑定参数

String sqlQuery = queryFragment.querySql(); // 生成 SQL 查询字符串
```

#### 3. QueryHelper

`QueryHelper` 提供了从对象构建 `QueryFragment` 的静态方法，特别是当对象包含分页信息时。

**使用案例：**

```java
UserRequest userRequest = new UserRequest();
userRequest.

setUsername("john");

Pageable pageable = PageRequest.of(0, 10);

QueryFragment queryFragment = QueryHelper.query(userRequest, pageable);
String sqlQuery = queryFragment.querySql();
```

#### 4. QueryJsonHelper

`QueryJsonHelper` 专注于处理 JSON 字段的查询，允许开发者构建针对 JSON 数据的 SQL 查询条件。

**使用案例：**

```java
Map<String, Object> jsonParams = new HashMap<>();
jsonParams.

put("extend.requestBody.nameEq","Test User");
jsonParams.

put("extend.emailEq","testuser@example.com");

QueryFragment queryFragment = QueryJsonHelper.queryJson(jsonParams, "a");
String sqlQuery = queryFragment.querySql();
```

#### 5. 全文搜索用例

对于全文搜索，可以使用 `QueryFragment` 的 `addQuery` 方法来添加全文搜索的条件。

**使用案例：**

```java
QueryFragment queryFragment = QueryFragment.withNew()
        .addColumn("id", "name", "email")
        .addQuery("users")
        .addWhere("to_tsvector('english', bio) @@ to_tsquery('english', :search)")
        .addOrder("ts_rank(to_tsvector('english', bio), to_tsquery('english', :search)) DESC");

queryFragment.

put("search","test user"); // 绑定全文搜索参数

String sqlQuery = queryFragment.querySql(); // 生成包含全文搜索的 SQL 查询字符串
```

在这个例子中，`to_tsvector` 和 `to_tsquery` 是 PostgreSQL 的全文搜索函数，用于将文本转换为向量和查询字符串。

#### 6. 集成 UserRequest

`UserRequest` 类扩展了 `User` 类，并添加了额外的属性和方法，用于处理用户请求。

**使用案例：**

```java
UserRequest userRequest = new UserRequest();
userRequest.

setUsername("john");
userRequest.

setSecurityCode("secure-code");

// 将 UserRequest 转换为 QueryFragment
QueryFragment queryFragment = userRequest.toParamSql();
String sqlQuery = queryFragment.querySql();
```

在这个例子中，`toParamSql` 方法将 `UserRequest` 对象转换为 `QueryFragment` 实例，以便构建 SQL 查询。

#### 7. 注意事项

- 确保在使用这些工具类时，数据库连接和配置已经正确设置。
- 对于全文搜索，确保数据库支持全文搜索功能，并且已经创建了相应的索引。
- 在绑定参数时，确保参数名称和值与查询中的占位符匹配。

通过这些工具类，开发者可以更方便地构建和执行 SQL 查询，同时保持代码的安全性和可维护性。
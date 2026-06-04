# Plate Platform

Plate Platform 是一个现代化的、基于 Spring Boot
4.0.0 的全栈 Web 应用程序平台，采用响应式编程模型和微服务架构设计。该平台提供了完整的用户管理、安全认证、日志记录、菜单管理、多租户支持等功能。

## 项目结构

该项目是一个 Gradle 多模块 Spring Boot 应用程序，主要应用入口位于`com.plate.boot`
包中。它包含处理不同方面的各种子包，如安全、关系数据处理和通用工具。项目遵循模块化架构，具有父"plate"项目和"platform"模块。

## 功能特性

该应用程序包含以下关键功能：

- **用户管理**: 完整的用户相关操作和认证功能
- **安全认证**: 基于 OAuth2 的健壮安全配置，支持 GitHub OAuth2 登录
- **日志记录**: 全面的日志记录功能，包含分页和清理功能
- **菜单管理**: 动态菜单项及其相关权限
- **多租户支持**: 内置租户支持，适用于 SaaS 应用
- **缓存机制**: 通过 Redis 缓存提升性能
- **响应式编程**: 基于 Spring WebFlux 构建的响应式、非阻塞操作
- **动态查询构建**: 自定义工具类（QueryHelper、QueryJsonHelper、QueryFragment）用于安全的 SQL 构建
- **全文搜索**: 支持中文全文搜索功能
- **JSON 数据处理**: PostgreSQL 的 JSON/JSONB 字段支持
- **并发控制**: 支持虚拟线程（Java 25）
- **CSRF 保护**: 防跨站请求伪造保护
- **会话管理**: 限制每个用户最多一个并发会话

## 技术栈

### 核心技术

- **Spring Boot**: 4.0.0
- **Spring WebFlux**: 响应式 Web 框架
- **Spring Data R2DBC**: 响应式数据库访问
- **Spring Security**: 安全框架
- **Spring Data Redis**: 响应式 Redis 数据访问
- **Spring Session**: 基于 Redis 的会话管理
- **Spring OAuth2**: OAuth2 客户端支持

### 数据库技术

- **PostgreSQL**: 主数据库
- **R2DBC PostgreSQL**: 响应式 PostgreSQL 驱动
- **Flyway**: 数据库迁移工具

### 其他技术

- **Redis**: 缓存和会话存储
- **Java**: 25 版本，启用虚拟线程
- **Gradle**: 构建工具
- **Lombok**: 代码简化
- **Log4j2**: 日志框架
- **Jackson**: JSON 处理
- **UUID Creator**: 时间有序 UUID 生成

## 依赖项列表

### 核心依赖

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

### 数据库相关

- PostgreSQL R2DBC 驱动
- PostgreSQL JDBC 驱动
- Flyway PostgreSQL 数据库插件

### 工具库

- Google Guava: 33.5.0
- UUID Creator: 6+
- Lombok

### 测试依赖

- Spring Boot Starter Test
- Reactor Test
- Spring Security Test
- Spring Boot Testcontainers
- Testcontainers JUnit Jupiter
- Testcontainers R2DBC
- Testcontainers PostgreSQL
- Redis Testcontainers

## 环境要求

### 开发环境

- **Java**: 25 或更高版本
- **Gradle**: 9.1 或更高版本
- **PostgreSQL**: 14 或更高版本（支持 UUIDv7 和中文全文搜索）
- **Redis**: 6.0 或更高版本

### 运行环境

- **操作系统**: 任何支持 Java 25 的系统
- **内存**: 建议至少 4GB RAM
- **存储**: 至少 1GB 可用空间

## 安装说明

### 前置条件

1. 安装 Java 25
2. 安装并运行 PostgreSQL 数据库
3. 安装并运行 Redis 服务器
4. 安装 Gradle

### 设置步骤

1. 克隆仓库

   ```bash
   git clone <repository-url>
   cd plate-platform
   ```

2. 设置 PostgreSQL 数据库（确保已启用 UUID 和中文全文搜索扩展）

   ```sql
   create extension if not exists "uuid-ossp";
   create extension if not exists pg_trgm;
   create extension if not exists zhparser;
   ```

3. 确保 Redis 服务器正在运行

4. 根据需要更新`platform/src/main/resources/application-local.yml`中的本地数据库和 Redis 配置

5. 构建项目
   ```bash
   ./gradlew build
   ```

## 使用方法

### 运行应用程序

要运行应用程序，请从根目录执行以下命令：

```bash
./gradlew :platform:bootRun
```

对于本地开发，使用本地配置文件：

```bash
./gradlew :platform:bootRun --args='--spring.profiles.active=local'
```

应用程序将在 `http://localhost:8080` 可访问。

### Docker 运行

构建 Docker 镜像：

```bash
./gradlew bootBuildImage
```

运行 Docker 容器：

```bash
docker run -p 8080:8080 alexbob/plate-platform:latest
```

## 测试基础设施

本项目使用 [Testcontainers](https://testcontainers.com/) 提供可靠且一致的测试环境。`InfrastructureConfiguration` 类配置了以下容器：

- **Redis**: 使用 `redis:latest` 镜像。
- **PostgreSQL**: 使用 `alexbob/postgres`（兼容标准 Postgres），并配置了特定的等待策略以确保数据库准备就绪。

这些容器在运行集成测试时会自动启动，确保测试是在真实的数据库和缓存实例上运行，而不是使用模拟对象。

````

## 配置选项

### 服务器配置

- `server.port`: 服务器端口（默认8080）
- `server.http2.enabled`: 启用HTTP/2（默认true）
- `server.shutdown`: 关机模式（默认graceful）
- `server.compression.enabled`: 启用压缩（默认true）

### Spring配置

- `spring.threads.virtual.enabled`: 启用虚拟线程（默认true）
- `spring.main.keep-alive`: 保持应用活跃（默认true）
- `spring.application.name`: 应用名称（默认plate）
- `spring.application.group`: 应用组（默认platform）

### WebFlux配置

- `spring.webflux.format.time`: 时间格式
- `spring.webflux.format.date-time`: 日期时间格式
- `spring.webflux.format.date`: 日期格式
- `spring.webflux.properties.path-prefixes`: 路径前缀配置

### Jackson配置

- `spring.jackson.date-format`: 日期格式
- `spring.jackson.time-zone`: 时区（默认GMT+8）
- `spring.jackson.locale`: 本地化设置（默认zh_CN）

### HTTP编解码器配置

- `spring.http.codecs.max-in-memory-size`: 最大内存大小（默认256KB）
- `spring.http.codecs.log-request-details`: 记录请求详情（默认false）

### 缓存配置

- `spring.cache.type`: 缓存类型（默认redis）
- `spring.cache.redis.key-prefix`: Redis键前缀（默认"plate:caches:"）
- `spring.cache.redis.time-to-live`: 过期时间（默认10分钟）
- `spring.cache.redis.use-key-prefix`: 使用键前缀（默认true）

### 会话配置

- `spring.session.timeout`: 会话超时（默认8小时）
- `spring.session.redis.cleanup-cron`: 清理Cron表达式（默认每5秒）

### R2DBC连接池配置

- `spring.r2dbc.pool.max-size`: 最大连接数（默认64）
- `spring.r2dbc.pool.max-idle-time`: 最大空闲时间（默认10分钟）
- `spring.r2dbc.pool.max-acquire-time`: 最大获取时间（默认30秒）
- `spring.r2dbc.pool.acquire-retry`: 获取重试次数（默认3）
- `spring.r2dbc.pool.validation-query`: 验证查询（默认SELECT 1）
- `spring.r2dbc.pool.max-validation-time`: 最大验证时间（默认2秒）
- `spring.r2dbc.pool.max-create-connection-time`: 最大创建连接时间（默认1秒）

### Redis配置

- `spring.data.redis.timeout`: 超时时间（默认30秒）
- `spring.data.redis.connect-timeout`: 连接超时（默认10秒）
- `spring.data.redis.repositories.enabled`: 启用仓库（默认false）

### Flyway配置

- `spring.flyway.baseline-on-migrate`: 迁移时建立基线（默认true）
- `spring.flyway.baseline-version`: 基线版本（默认1.0.0）
- `spring.flyway.baseline-description`: 基线描述

### OAuth2配置

- `spring.security.oauth2.client.registration.github.client-id`: GitHub客户端ID

## API文档

API文档可以通过Swagger UI访问。启动应用程序后，可以访问以下地址：

`http://localhost:8080/swagger-ui.html`

### 主要API端点

#### 安全相关端点

- `/oauth2/login` - 获取登录令牌
- `/oauth2/csrf` - 获取CSRF令牌
- `/oauth2/bind` - 绑定OAuth2客户端
- `/oauth2/change/password` - 更改密码
- `/oauth2/logout` - 注销

#### 日志相关端点

- `/loggers/page` - 分页获取日志记录

#### 菜单相关端点

- `/rel/menus` - 菜单管理相关操作

#### 安全相关端点

- `/sec/users` - 用户管理
- `/sec/groups` - 用户组管理
- `/sec/tenants` - 租户管理
- `/sec/authorities` - 权限管理

## 示例代码

### 使用QueryHelper进行查询

```java
// 创建查询片段
QueryFragment queryFragment = QueryFragment.from("users")
                .column("id", "name", "email")
                .where("age > :age")
                .orderBy("name ASC");

// 绑定参数
queryFragment.

put("age",18);

// 生成SQL查询
String sqlQuery = queryFragment.querySql();
````

### 使用 QueryHelper 构建查询

```java
// 创建用户请求对象
UserRequest userRequest = new UserRequest();
userRequest.

setUsername("john");

// 创建分页对象
Pageable pageable = PageRequest.of(0, 10);

// 构建查询片段
QueryFragment queryFragment = QueryHelper.query(userRequest, pageable);
String sqlQuery = queryFragment.querySql();
```

### 使用 QueryJsonHelper 处理 JSON 查询

```java
// 创建JSON参数映射
Map<String, Object> jsonParams = new HashMap<>();
jsonParams.

put("extend.requestBody.nameEq","Test User");
jsonParams.

put("extend.emailEq","testuser@example.com");

// 构建JSON查询条件
QueryFragment.Condition condition = QueryJsonHelper.queryJson(jsonParams, "a");
QueryFragment queryFragment = QueryFragment.conditional(condition);
String sqlQuery = queryFragment.querySql();
```

### 全文搜索示例

```java
// 使用全文搜索
QueryFragment queryFragment = QueryFragment.from("users")
                .column("id", "name", "email")
                .ts("bio", "test user"); // 添加全文搜索条件

String sqlQuery = queryFragment.querySql();
```

## 安全特性

### 认证和授权

- OAuth2 客户端支持（包括 GitHub 登录）
- 基于角色的访问控制（RBAC）
- 支持管理员角色（ROLE_SYSTEM_ADMINISTRATORS）
- 密码编码使用 DelegatingPasswordEncoder（默认 bcrypt）

### 会话管理

- 限制每个用户最多一个并发会话
- 会话超时设置（默认 8 小时）
- Redis 支持的分布式会话

### CSRF 保护

- Cookie-based CSRF 令牌存储
- 特定端点排除 CSRF 保护（如 OAuth2 相关端点）

### 密码安全

- 支持多种密码编码算法（bcrypt、argon2、pbkdf2、scrypt 等）
- 密码强度验证
- 防止相同新旧密码更改

## 数据库设计

### 核心表结构

#### 用户表 (se_users)

- 用户基本信息存储
- 支持多租户
- 密码安全存储
- JSON 扩展字段
- 全文搜索向量

#### 权限表 (se_authorities)

- 用户权限关联
- 支持多租户
- JSON 扩展字段

#### 用户组表 (se_groups)

- 用户组管理
- 支持层级结构
- JSON 扩展字段
- 全文搜索向量

#### 组权限表 (se_group_authorities)

- 用户组权限关联
- JSON 扩展字段

#### 组成员表 (se_group_members)

- 用户组成员关系
- JSON 扩展字段

#### 租户表 (se_tenants)

- 租户信息管理
- 支持层级结构
- JSON 扩展字段
- 全文搜索向量

#### 租户成员表 (se_tenant_members)

- 租户用户关系
- JSON 扩展字段

#### 菜单表 (se_menus)

- 菜单权限管理
- 支持层级结构
- 多租户支持
- JSON 扩展字段
- 全文搜索向量

#### 日志表 (se_loggers)

- 操作日志记录
- 支持多租户
- JSON 上下文字段
- 全文搜索向量

### 数据库特性

- 使用 UUIDv7 作为主键
- 自动更新时间戳触发器
- 中文全文搜索支持
- JSON/JSONB 字段支持
- GIN 索引优化查询

## 开发指南

### 代码约定

1. 使用 ContextUtils.OBJECT_MAPPER 进行 JSON 操作，而不是创建新的 ObjectMapper 实例
2. 使用 ContextUtils.nextId()生成时间有序 UUID
3. 在响应式代码中使用 ContextUtils.securityDetails()访问安全上下文
4. 使用 ContextUtils.getClientIpAddress()提取请求 IP 地址（考虑代理头）
5. 使用@RequiredArgsConstructor 进行依赖注入（final 字段）
6. 使用@Log4j2 注解而不是手动创建 logger
7. 使用 ContextUtils.createDelegatingPasswordEncoder()提供的 PasswordEncoder
8. 存储库路径使用"rel"前缀表示关系端点，"sec"前缀表示安全端点

### 响应式编程

- 正确使用 Mono/Flux 类型
- 避免阻塞操作
- 使用虚拟线程提高并发性能

### 安全实践

- 使用预编译语句防止 SQL 注入
- 正确处理 CSRF 令牌
- 验证用户输入
- 使用安全的密码编码

### 数据访问

- 使用 QueryHelper、QueryJsonHelper 和 QueryFragment 进行安全查询构建
- 利用 PostgreSQL 的 JSON 功能
- 使用全文搜索优化查询性能

## 贡献指南

欢迎贡献！如果遇到任何问题或希望添加新功能，请随时提交问题或拉取请求。

### 开发流程

1. Fork 仓库
2. 创建功能分支
3. 提交更改
4. 确保所有测试通过
5. 提交拉取请求

### 代码风格

- 遵循现有的代码风格和约定
- 为新功能编写测试
- 在提交拉取请求之前确保所有测试通过
- 根据需要更新文档
- 使用提供的查询构建工具（QueryHelper、QueryJsonHelper、QueryFragment）进行数据库操作以防止 SQL 注入

### 测试要求

- 为新功能添加单元测试
- 运行集成测试确保兼容性
- 验证安全功能正常工作

## 构建和测试

### 构建项目

构建整个项目：

```bash
./gradlew build
```

仅构建平台模块：

```bash
./gradlew :platform:build
```

### 运行测试

运行所有测试：

```bash
./gradlew test
```

运行平台模块测试：

```bash
./gradlew :platform:test
```

运行特定测试类：

```bash
./gradlew :platform:test --tests "TestClassName"
```

## SSL 证书生成

> 参考: https://github.com/FiloSottile/mkcert.git

为本地开发生成 SSL 证书：

```bash
mkcert -cert-file localhost+2.pem -key-file localhost+2-key.pem -pkcs12 plate

keytool -importkeystore -srckeystore plate.p12 -srcstoretype pkcs12 -srcalias 1 -destkeystore plate.jks -deststoretype jks -deststorepass 123456 -destalias plate
```

在`application-local.yml`中取消 SSL 配置的注释以启用 SSL。

## 部署指南

### 生产环境部署

1. 配置生产数据库连接
2. 配置生产 Redis 连接
3. 设置环境变量（数据库密码、Redis 密码等）
4. 构建生产镜像或 JAR 文件
5. 启动应用

### 环境变量配置

- `port`: 服务器端口（默认 8080）
- `github.client-id`: GitHub OAuth2 客户端 ID
- 数据库连接参数
- Redis 连接参数
- 安全相关的密钥和密码

## 性能优化

### 虚拟线程

- 启用了 Java 25 的虚拟线程支持
- 提高 I/O 密集型操作的并发性能

### 缓存策略

- Redis 缓存支持
- 10 分钟默认 TTL
- 前缀键策略

### 数据库优化

- R2DBC 连接池配置
- 64 个最大连接数
- 自动连接验证

## 监控和运维

### Actuator 端点

- 健康检查
- 指标监控
- 环境信息
- 配置属性

### 日志管理

- Log4j2 日志框架
- 结构化日志输出
- 分级日志记录

## 常见问题解答

### Q: 如何配置数据库连接？

A: 在 application.yml 或环境变量中配置数据库连接参数，包括 URL、用户名和密码。

### Q: 如何添加新的 OAuth2 提供商？

A: 在 application.yml 中添加相应的客户端注册配置。

### Q: 如何自定义用户权限？

A: 使用用户组和权限管理功能，通过 se_groups、se_group_authorities 和 se_authorities 表进行配置。

### Q: 如何扩展用户信息？

A: 使用 JSON 扩展字段（extend）来存储自定义用户数据。

### Q: 如何实现多租户数据隔离？

A: 系统内置多租户支持，所有数据表都包含 tenant_code 字段，通过安全上下文自动过滤。

## 联系方式

项目作者: Alex Bob
GitHub: https://github.com/vnobo

## 项目状态

该项目正在积极开发中。当前版本提供了完整的用户管理、安全认证、日志记录和多租户支持功能。我们持续改进和添加新功能以满足企业级应用需求。

## 许可证信息

本项目采用 MIT 许可证。

## 版本历史

### V1.0

- 基线版本
- 基础用户管理功能
- 安全认证系统
- 多租户支持

### V1.0.1

- 扩展功能
- 性能优化

### V1.0.2

- 数据库模式定义
- 完整的表结构设计
- 全文搜索支持

### V1.0.3

- 数据初始化
- 基础数据配置

### V1.0.4

- 测试数据初始化
- 完整功能验证

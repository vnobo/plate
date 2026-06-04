# Plate Platform — 后端

基于 Spring Boot 4.0.6 的响应式后端，使用 WebFlux、R2DBC 和 PostgreSQL。

## 技术栈

- **Spring Boot** 4.0.6（WebFlux、R2DBC、Security、Session）
- **Java** 25（启用虚拟线程）
- **PostgreSQL** 17+（响应式驱动）
- **Redis** 7.0+（缓存和会话）
- **Flyway** 数据库迁移
- **GraalVM** 原生镜像支持（构建工具 0.11.5）
- **Lombok** 减少样板代码
- **springdoc-openapi** API 文档

## 构建

```bash
./gradlew build          # 编译 + 测试
./gradlew bootRun        # 本地运行
./gradlew bootBuildImage # 构建 Docker 镜像
```

## 项目结构

```
boot/platform/src/main/java/com/plate/boot/
├── security/              # 认证与授权
│   ├── core/user/         # 用户管理、事件
│   ├── core/group/        # 分组管理、权限、成员
│   ├── core/tenant/       # 多租户管理
│   ├── captcha/           # 验证码生成与验证
│   └── oauth2/            # OAuth2 成功处理和用户服务
├── relational/            # 业务逻辑
│   ├── menus/             # 菜单 CRUD 及事件
│   ├── logger/            # 审计日志（定时清理）
│   └── dictionaries/      # 字典管理
├── commons/               # 公共工具
│   ├── base/              # AbstractEntity、AbstractCache、AbstractEvent
│   ├── query/             # QueryFragment、QueryHelper、QueryJsonHelper
│   ├── converters/        # R2DBC 类型转换器
│   ├── exception/         # 全局异常处理和自定义异常
│   └── utils/             # BeanUtils、ContextUtils、DatabaseUtils
└── config/                # Spring 配置
    ├── SecurityConfiguration
    ├── R2dbcConfiguration
    ├── RedisConfiguration
    ├── SessionConfiguration
    └── WebConfiguration
```

## 配置

主配置文件：`src/main/resources/application.yml`

主要特性：
- R2DBC 连接池（最大 64 连接）
- Redis 会话管理（8 小时超时）
- Flyway 基线迁移
- API 版本控制（通过 `x-api-version` 请求头）
- 路径前缀：`/sec/*`（安全）、`/rel/*`（业务）

## 测试

```bash
./gradlew test           # JUnit 5 + Testcontainers（PostgreSQL + Redis）
```

## API 文档

运行时访问 `/swagger-ui.html` 查看 Swagger UI。

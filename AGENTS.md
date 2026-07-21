# AGENTS.md

> Plate Platform — 响应式多租户后台管理系统（Polyglot Monorepo）。
> 本文档为 AI Agent 提供高信号量的项目上下文，帮助快速上手、避免踩坑。
>
> - **后端** (`boot/`): Spring Boot 4.1 / WebFlux / R2DBC / Redis — Java 25, Gradle 9.5.x
> - **前端** (`ui/ng-plate/`): Angular 22 SSR — TypeScript 6, pnpm 11.12.0, Tabler UI

---

## 1. 开发命令速查

### 后端（在 `boot/` 目录执行）

```bash
./gradlew :platform:bootRun --args='--spring.profiles.active=local'  # 本地启动 (port 8080)
./gradlew :platform:test                                              # Platform 测试（需 Docker）
./gradlew :platform:test --tests "com.plate.boot.security.SecurityManagerTest"  # 单个测试类
./gradlew :platform:test --tests "*MethodName*"                       # 单个测试方法
./gradlew :platform:compileJava                                       # 仅编译，跳过测试
./gradlew build                                                        # 完整构建 + 测试
./gradlew bootBuildImage                                              # Buildpacks 构建 OCI 镜像
```

### 前端（在 `ui/ng-plate/` 目录执行）

```bash
pnpm install          # 安装依赖
pnpm start            # 开发服务器 → http://localhost:4200（代理 /api → localhost:8080）
pnpm build            # 生产构建（SSR + CSR，输出 dist/）
pnpm test             # Vitest 单元测试
```

### 开发环境默认凭据

- 后端 Swagger UI: `http://127.0.0.1:8080/swagger-ui.html`
- 管理员: `admin` / `123456`，普通用户: `user` / `123456`
- PostgreSQL: `127.0.0.1:5432/plate`，用户 `farmer` / `123456`
- Redis: `127.0.0.1`

---

## 2. Monorepo 结构

```
plate/
├── AGENTS.md                    # 本文件
├── CLAUDE.md                    # 项目总览指引（英文）
├── boot/                        # 后端（Gradle 单模块构建）
│   ├── CLAUDE.md                # 后端专属指引（英文）
│   ├── build.gradle             # 根构建脚本（插件/依赖/Java 25 toolchain）
│   ├── settings.gradle          # rootProject "plate", include :platform
│   ├── gradle.properties        # version=4.1.0, graalvm, guava, springdoc 版本
│   └── platform/                # 唯一子模块 — 全部后端代码
│       └── src/
│           ├── main/java/com/plate/boot/
│           │   ├── BootApplication.java       # Spring Boot 入口 (@SpringBootApplication)
│           │   ├── commons/                   # 公共基础层
│           │   ├── config/                    # 基础设施配置层
│           │   ├── relational/                # 业务域（字典/日志/菜单）
│           │   └── security/                  # 安全域（用户/组/租户/认证）
│           ├── main/resources/
│           │   ├── application.yml            # 生产/默认配置
│           │   ├── application-local.yml      # 本地开发覆盖（git-ignored）
│           │   └── db/migration/              # Flyway 迁移脚本 (V1.0.0–V1.0.6)
│           └── test/                          # 测试（Testcontainers 集成测试 + 单元测试）
└── ui/ng-plate/                 # 前端（Angular 22 SSR 独立项目）
    ├── AGENTS.md                # 前端编码规范（Angular/TypeScript 最佳实践）
    ├── angular.json             # 构建配置（pnpm, SCSS, Tabler CSS/JS, SSR）
    ├── proxy.conf.json          # 开发代理：/api → http://localhost:8080/（去除 /api 前缀）
    └── src/app/
        ├── core/                # HTTP 拦截器、路由守卫、Token 服务、存储封装
        ├── layout/              # BaseLayout（侧边栏+头部）、BlankLayout（登录页）
        ├── pages/               # 懒加载页面模块
        └── plugins/             # 可复用 UI 组件（DataTable, Modals, Toasts, Transfer）
```

**关键事实**: 后端和前端是完全独立的构建系统（Gradle vs pnpm/Angular CLI），无共享 workspace 配置。无 pre-commit hooks、无 ESLint 配置。

---

## 3. 技术栈

| 类别 | 技术 | 版本/说明 |
|------|------|-----------|
| **后端语言** | Java | 25（虚拟线程、GraalVM 支持） |
| **前端语言** | TypeScript | 6.x |
| **后端构建** | Gradle | 9.5.x（Wrapper） |
| **前端构建** | pnpm | 11.5.2 |
| **Web 框架** | Spring Boot WebFlux | 4.1.0（响应式非阻塞, Netty HTTP/2） |
| **前端框架** | Angular | 22.x（SSR + Signals + Zoneless） |
| **数据库** | PostgreSQL | 17+（uuid-ossp, pg_trgm, zhparser 扩展） |
| **数据访问** | Spring Data R2DBC | 响应式关系数据库 |
| **缓存/会话** | Redis | 7.0+（缓存 + WebSession） |
| **安全** | Spring Security | Session-based + OAuth2 + CSRF Cookie |
| **迁移** | Flyway | baseline-on-migrate, V1.0.0–V1.0.6 |
| **JSON** | Jackson 3.x (tools.jackson) | 使用 `ContextUtils.OBJECT_MAPPER` |
| **UI** | Tabler UI | @tabler/core CSS/JS |
| **日志** | Log4j2 | `@Log4j2` 注解（Logback 已排除） |
| **测试** | JUnit 5 + Testcontainers | 真实 PostgreSQL + Redis 容器 |
| **前端测试** | Vitest | @angular/build:unit-test |
| **SSR** | Angular SSR + Express 5.1.0 | 增量水合 (Incremental Hydration) |

---

## 4. 后端架构

### 4.1 模块职责

| 模块 | 路径 | 职责 |
|------|------|------|
| **commons/** | `com.plate.boot.commons` | 公共基类、动态 SQL 构建器、工具类、类型转换器、异常定义 |
| **config/** | `com.plate.boot.config` | R2DBC/Redis/Session/Security/Web 基础设施配置 |
| **security/** | `com.plate.boot.security` | 用户/组/租户 CRUD、认证、权限、验证码、OAuth2 |
| **relational/** | `com.plate.boot.relational` | 字典管理、菜单管理、审计日志记录 |

### 4.2 commons/ 子模块

| 子包 | 核心类 | 职责 |
|------|--------|------|
| `base/` | `AbstractEntity`, `AbstractCache`, `AbstractEvent`, `BaseEntity`, `BaseView` | 实体基类（`code` UUIDv7 PK, `tenantCode`, `extend` JSONB, `version` 乐观锁, 审计字段） |
| `query/` | `QueryFragment`, `QueryHelper`, `QueryJsonHelper` | 动态 SQL 构建器（`from()`, `where()`, `in()`, `like()`, `ts()` 全文搜索） |
| `utils/` | `ContextUtils`, `DatabaseUtils`, `BeanUtils` | 全局工具：`securityDetails()` 当前用户, `nextId()` UUIDv7, `eventPublisher()` 事件发布, `OBJECT_MAPPER` |
| `converters/` | `JsonNodeConverters`, `UserAuditorConverters` | R2DBC 类型转换（JSONB ↔ JsonNode） |
| `exception/` | `RestServerException`, `QueryException`, `JsonException` | 异常体系，由 `GlobalExceptionHandler` 统一处理 |

### 4.3 config/ 子模块

| 类 | 职责 |
|----|------|
| `SecurityConfiguration` | SecurityWebFilterChain: CSRF → 认证 → 并发会话控制 → Logout |
| `WebConfiguration` | 路径前缀映射：`/rel` → relational 包, `/sec` → security 包 |
| `R2dbcConfiguration` | R2DBC 连接配置，注册类型转换器 |
| `RedisConfiguration` | Redis 连接与缓存配置 |
| `SessionConfiguration` | Redis WebSession（8h TTL，单用户单会话 SessionLimit.of(1)） |
| `WebfluxProperties` | 路径前缀、API 版本、分页参数 |
| `HttpCodecsProperties` | maxInMemorySize（默认 256KB） |

### 4.4 security/ 子模块

| 子包 | 核心类 | 职责 |
|------|--------|------|
| 根 | `SecurityManager`, `SecurityController`, `SecurityDetails`, `CsrfWebFilter` | 认证核心：`findByUsername()`（大小写不敏感）, 缓存用户权限/组/租户信息 |
| `core/user/` | `User`, `UsersService`, `UsersController` | 用户 CRUD + `authority/` 子包管理用户权限 |
| `core/group/` | `Group`, `GroupsService`, `GroupsController` | 用户组 CRUD（`pcode` 层级结构）+ `authority/` + `member/` |
| `core/tenant/` | `Tenant`, `TenantsService`, `TenantsController` | 租户 CRUD（`pcode` 层级结构）+ `member/` |
| `captcha/` | `CaptchaController`, `CaptchaRepository` | 验证码（Redis 存储，免认证 `permitAll`） |
| `oauth2/` | `Oauth2UserService`, `Oauth2SuccessHandler` | GitHub OAuth2 自动注册/绑定，XHR-aware 响应 |

**每个特性包的标准结构**: Entity → `*Req`（请求 DTO）→ `*Res`（响应 DTO）→ `*Event`（领域事件）→ `*Service`（继承 AbstractCache）→ `*Repository`（R2DBC）→ `*Controller`（返回 Mono/Flux）

### 4.5 relational/ 子模块

| 子包 | 核心类 | 职责 |
|------|--------|------|
| 根 | `LoggerFilter`, `MethodType` | 审计日志拦截器：匹配非安全 HTTP 方法，缓存 req/resp DataBuffer，异步发布 LoggerEvent |
| `dictionaries/` | `Dictionary`, `DictionariesService/Controller` | 字典 CRUD（`pcode` 层级, `dict_type` 分类） |
| `logger/` | `Logger`, `LoggersService/Controller` | 审计日志查询（由 LoggerFilter 事件驱动写入） |
| `menus/` | `Menu`, `MenusService/Controller` | 菜单管理（`type`: FOLDER/MENU/LINK/API） |

### 4.6 请求处理管道

```
HTTP Request (Netty, port 8080, HTTP/2)
        │
        ▼
  LoggerFilter ── 匹配非安全方法 → 缓存 req/resp → 异步发布 LoggerEvent
        │
        ▼
  Spring Security Chain:
    CsrfWebFilter → AuthenticationWebFilter → ConcurrentSessionControl(1) → Logout
        │
        ▼
  WebConfiguration 路径前缀路由:
    /rel/** → com.plate.boot.relational
    /sec/** → com.plate.boot.security
    /oauth2/** → SecurityController
        │
        ▼
  Controller → Service (AbstractCache, Redis 缓存, 事件发布) → Repository (R2DBC) → PostgreSQL
```

### 4.7 认证流程

1. `GET /oauth2/csrf` → CsrfWebFilter 写入 Reactor Context → Cookie `XSRF-TOKEN`
2. `GET /oauth2/login` → Basic Auth 或已认证 Session → `SecurityManager.findByUsername()`
3. SecurityManager: 加载用户（忽略大小写）→ 合并用户直接权限 + 组继承权限 → 加载组/租户 → 组装 SecurityDetails
4. 响应 → `Set-Cookie: SESSION=...`（Redis-backed WebSession）
5. 后续请求携带 SESSION cookie 自动恢复 SecurityContext
6. `POST /oauth2/logout` → Clear-Site-Data 响应头

**SecurityManager 缓存键**（TTL 10min）: `OAUTH2_{bindType}_{openid}`, `USER_GROUPS-{userCode}`, `USER_TENANTS-{userCode}`, `USER_AUTHORITIES-{userCode}`, `GROUP_AUTHORITIES-{userCode}`

---

## 5. 前端架构

### 5.1 关键配置

- **Zoneless** 变更检测（Signals 驱动，无 Zone.js）
- **SSR** + 增量水合（`withIncrementalHydration`）
- **XSRF** 保护: cookie `XSRF-TOKEN` → header `X-XSRF-TOKEN`
- **PWA**: Service Worker 仅生产环境启用
- **路径别名**: `@app/` → `src/app/`, `@envs/` → `src/envs/`, `@styles/` → `src/styles/`
- **环境文件**: `env.ts`（生产: `host: ''`, 直接调用 `/rel`/`/sec`）vs `env.dev.ts`（开发: `host: '/api'`, 走代理）
- **样式**: SCSS，`@tabler/core` 全局 CSS/JS
- **国际化**: `zh-Hans` 区域，dayjs 中文 locale

### 5.2 路由结构

| 路由 | 页面 | 布局 |
|------|------|------|
| `/passport` | 登录、锁屏 | BlankLayout |
| `/dashboard` | 欢迎页、用户管理（列表+表单） | BaseLayout |
| `/platform` | 租户管理 | BaseLayout |
| `/examples` | 数据表格示例、穿梭框示例 | BaseLayout |
| `/error` | 404, 500, 512 | — |
| `/` → `/passport` | 重定向 | — |
| `/**` → `/error` | 兜底 | — |

### 5.3 前端编码约定

- 独立组件（standalone），**不要**设置 `standalone: true`（Angular 22 默认值）
- 使用 `inject()` 注入服务，不要用构造器注入
- Signals 管理状态：`signal()`, `computed()`, `input()`, `output()`
- **不要** `mutate` signals，用 `update` 或 `set`
- 原生控制流 `@if`/`@for`/`@switch`，不要用 `*ngIf`/`*ngFor`
- **不要** `@HostBinding`/`@HostListener`，用 `host` 对象
- **不要** `ngClass`/`ngStyle`，用 `class`/`style` 绑定
- 外部模板/样式路径相对于组件 TS 文件
- Reactive forms，不要 Template-driven forms

---

## 6. 模块依赖规则

```
Config Agent ──┐
               ▼
Commons Agent ──┘  (base/query/utils/converters/exception)
       │
  ┌────┴────┐
  ▼         ▼
Security   Relational   （相互独立，无交叉依赖）
  │         │
  └────┬────┘
       ▼
   Frontend Agent (Angular UI)
```

| 规则 | 状态 |
|------|------|
| `commons/` → `security/` 或 `relational/` | ❌ **禁止** |
| `config/` → 业务 Controller/Service 方法调用 | ❌ **禁止** |
| `security/` ↔ `relational/` | ❌ **禁止交叉依赖** |
| `security/` → `commons/` | ✅ 允许 |
| `relational/` → `commons/` | ✅ 允许 |
| 所有模块 → `ContextUtils` | ✅ 允许 |

---

## 7. 数据模型

所有表前缀 `se_`。通用列：`code` (UUIDv7 PK), `version` (乐观锁), `tenant_code` (多租户隔离), `extend` (JSONB 扩展), `created_by/updated_by` (UUID), `created_at/updated_at` (TIMESTAMPTZ), `text_search` (tsvector GIN 索引, zhparser 中文分词)。

| 表 | 关键列 | 说明 |
|----|--------|------|
| `se_users` | `username`, `password`, `phone`, `email`, `name`, `disabled`, `account_expired/locked`, `credentials_expired`, `login_time` | 用户表 |
| `se_authorities` | `user_code` (FK), `authority` | 用户直接权限 |
| `se_groups` | `pcode` (父组), `name`, `description` | 用户组（层级结构） |
| `se_group_authorities` | `group_code` (FK), `authority` | 组权限 |
| `se_group_members` | `group_code` (FK), `user_code` (FK) | 组成员 |
| `se_tenants` | `pcode` (父租户), `name`, `description` | 租户（层级结构, id 为 serial 非 BIGSERIAL） |
| `se_tenant_members` | `tenant_code` (FK), `user_code` (FK), `enabled` | 租户成员 |
| `se_menus` | `pcode`, `type` (FOLDER/MENU/LINK/API), `authority`, `name`, `path` | 菜单（层级 + 类型枚举） |
| `se_loggers` | `prefix`, `operator`, `status`, `method`, `url`, `context` (JSONB) | 审计日志（由 LoggerFilter 事件驱动写入） |
| `se_dictionaries` | `pcode`, `dict_type`, `dict_key`, `dict_value`, `dict_label`, `description`, `sort_no`, `enabled` | 数据字典（`tenant_code + dict_type + dict_key` 唯一约束） |
| `oauth2_authorized_client` | `client_registration_id`, `principal_name`, `access_token_*` | OAuth2 令牌存储 |

**注意**: `se_tenants.id` 使用 `serial`（非 BIGSERIAL），与其他表不同。

---

## 8. API 合约

### 认证 API (`/oauth2/**`)

| 方法 | 路径 | 认证 | 说明 |
|------|------|------|------|
| `GET` | `/oauth2/login` | Basic Auth / Session | 登录，返回 `AuthenticationToken` |
| `GET` | `/oauth2/csrf` | Session | 获取 CSRF Token |
| `GET` | `/oauth2/bind` | Session | OAuth2 绑定查询 |
| `POST` | `/oauth2/change/password` | Session | 改密 `{password, newPassword}` |
| `POST` | `/oauth2/logout` | Session | 登出 + Clear-Site-Data |

### 安全业务 API (`/sec/**`)

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET` | `/sec/users/search` | 搜索用户 |
| `GET` | `/sec/users/page` | 分页查询 |
| `POST` | `/sec/users` | 创建用户 |
| `PUT` | `/sec/users` | 更新用户 |
| `DELETE` | `/sec/users/{code}` | 删除用户 |
| `*` | `/sec/users/authorities/**` | 用户权限 |
| `*` | `/sec/groups/**` | 用户组 CRUD |
| `*` | `/sec/groups/authorities/**` | 组权限 |
| `*` | `/sec/groups/members/**` | 组成员 |
| `*` | `/sec/tenants/**` | 租户 CRUD |
| `*` | `/sec/tenants/members/**` | 租户成员 |
| `GET` | `/sec/captcha/code` | 验证码（免认证） |

### 关系业务 API (`/rel/**`)

| 方法 | 路径 | 说明 |
|------|------|------|
| `*` | `/rel/dictionaries/**` | 字典管理 |
| `GET` | `/rel/loggers/**` | 审计日志查询 |
| `*` | `/rel/menus/**` | 菜单管理 |

**请求要求**: POST/PUT/DELETE 必须携带 Cookie `SESSION` + Header `X-XSRF-TOKEN`。API 版本通过 `x-api-version` Header 或 `apiVersion` Query 参数控制（默认 `v1`）。

---

## 9. 代码约定

### 后端

| 规则 | 说明 |
|------|------|
| **JSON** | 始终使用 `ContextUtils.OBJECT_MAPPER`，**禁止** `new ObjectMapper()` |
| **主键** | `ContextUtils.nextId()`（UUIDv7） |
| **当前用户** | `ContextUtils.securityDetails()` → `Mono<SecurityDetails>`，**禁止** `SecurityContextHolder` |
| **事件发布** | `ContextUtils.eventPublisher(AbstractEvent)` |
| **动态 SQL** | 使用 `QueryFragment`/`QueryHelper`/`QueryJsonHelper`，**禁止**字符串拼接 |
| **Service 基类** | 继承 `AbstractCache` 获得 `queryWithCache()`/`countWithCache()`（Redis 前缀 `plate:caches:`, TTL 10min） |
| **响应式** | Controller 必须返回 `Mono<T>`/`Flux<T>`，**禁止**阻塞 IO |
| **DTO** | `*Req` 请求 DTO，`*Res` 响应 DTO（**禁止**暴露 password；`UserRes` 脱敏 phone/email） |
| **层级结构** | Group/Tenant/Dictionary/Menu 均使用 `pcode`（父节点 code） |
| **路径前缀** | `/rel/` → relational 包, `/sec/` → security 包（由 `WebConfiguration` 自动绑定） |
| **权限** | `@PreAuthorize("hasRole('...')")`；管理员角色常量 `ContextUtils.RULE_ADMINISTRATORS` |
| **DI** | Lombok `@RequiredArgsConstructor` + `final` 字段 |
| **日志** | `@Log4j2` 注解，**禁止** `System.out` 或 Logback |
| **密码** | `DelegatingPasswordEncoder`（默认 bcrypt） |
| **缓存阈值** | 对象超过 `HttpCodecsProperties.maxInMemorySize`（256KB）不缓存 |
| **并发会话** | 单用户单会话（`SessionLimit.of(1)`），后登录踢前者 |

### 前端

| 规则 | 说明 |
|------|------|
| **组件** | 独立组件（standalone），不要设 `standalone: true` |
| **DI** | `inject()` 函数，不要构造器注入 |
| **状态** | Signals（`signal()`, `computed()`, `input()`, `output()`） |
| **模板** | 原生控制流 `@if`/`@for`/`@switch`，不要结构型指令 |
| **HTTP** | Angular HttpClient，拦截器自动处理 XSRF + 认证 |
| **路由** | 懒加载（`loadChildren`），组件输入绑定 |
| **样式** | SCSS + @tabler/core |
| **测试** | Vitest（`@angular/build:unit-test`），文件 `*.spec.ts` |
| **host 绑定** | 用 `host` 对象，不要 `@HostBinding`/`@HostListener` |
| **信号操作** | 用 `update`/`set`，不要 `mutate` |

---

## 10. Flyway 迁移

脚本位置：`boot/platform/src/main/resources/db/migration/`

| 脚本 | 内容 |
|------|------|
| `V1.0.0__Baseline.sql` | 基线 |
| `V1.0.1__Extension.sql` | PostgreSQL 扩展（uuid-ossp, pg_trgm, zhparser） |
| `V1.0.2__Schema.sql` | 创建所有 se_* 表 + updated_at 触发器函数 |
| `V1.0.3__Data.sql` | 初始化数据 |
| `V1.0.4__InitTestData.sql` | 测试数据 + `.conf` 配置文件 |
| `V1.0.5__Dictionary.sql` | 字典表结构 |
| `V1.0.6__DictionaryData.sql` | 字典初始数据 |

**规则**: `baseline-on-migrate: true`, `baseline-version: 1.0.0`。新表必须写新的 `V1.x.y__*.sql`。**严禁**修改已有迁移文件（Flyway checksum 校验）。

---

## 11. 测试

### 后端

- **集成测试**: `ApplicationTests.java`（21 个测试, `@SpringBootTest(webEnvironment = RANDOM_PORT)`, `WebTestClient`）
- **单元测试**: `SecurityManagerTest.java`, `SecurityControllerTest.java`（Mockito + StepVerifier）
- **基础设施**: `InfrastructureConfiguration.java`（Testcontainers: `alexbob/postgres` + zhparser, Redis）
- **测试配置**: `src/test/resources/application.yml`（缓存 TTL 5min, maxInMemorySize 10MB, debug 日志）
- **前提**: Docker daemon 必须运行

```bash
cd boot
./gradlew :platform:test                            # 全部测试
./gradlew :platform:test --tests "*ClassName*"       # 单个类
./gradlew :platform:test --tests "*MethodName*"      # 单个方法
```

### 前端

```bash
cd ui/ng-plate
pnpm test                                            # Vitest 单元测试
```

---

## 12. 配置属性速查

| 属性 | 值 | 说明 |
|------|-----|------|
| `server.port` | `8080` | HTTP 端口 |
| `server.http2.enabled` | `true` | HTTP/2 |
| `spring.threads.virtual.enabled` | `true` | Java 虚拟线程 |
| `spring.session.timeout` | `8H` | 会话有效期 |
| `spring.cache.redis.key-prefix` | `plate:caches:` | 缓存前缀 |
| `spring.cache.redis.time-to-live` | `10m` | 缓存 TTL（测试 5min） |
| `spring.http.codecs.max-in-memory-size` | `256KB` | 内存缓冲上限（测试 10MB） |
| `spring.r2dbc.pool.max-size` | `64` | 连接池上限 |
| `spring.jackson.time-zone` | `GMT+8` | 时区 |
| `spring.jackson.locale` | `zh_CN` | 区域 |

---

## 13. CI/CD

GitHub Actions 工作流 (`.github/workflows/`)：

| 文件 | 触发条件 | 职责 |
|------|---------|------|
| `gradle-tests.yml` | push to `main`/`dev` | 运行 `./gradlew test`（JDK 25 Liberica） |
| `gradle-build.yml` | push to `main`/`dev` + tags `v*` + releases created | 多架构 OCI 镜像 (amd64+arm64) via Buildpacks → GHCR + Docker Hub |
| `cleanup-caches.yml` | PR close | 清理 PR 缓存 |

镜像发布：`ghcr.io/<actor>/plate-platform` 和 `docker.io/alexbob/plate-platform`

---

## 14. 环境要求

| 组件 | 版本 | 说明 |
|------|------|------|
| Java | 25+ | Liberica JDK 推荐 |
| Gradle | 9.5+ | Wrapper 已包含 |
| PostgreSQL | 17+ | uuid-ossp, pg_trgm, zhparser 扩展 |
| Redis | 7.0+ | 缓存 + WebSession |
| Docker | 最新 | Testcontainers 测试 |
| Node.js | LTS | 前端开发 |
| pnpm | 11.5.2 | 前端包管理 |

---

## 15. 相关文档

| 文档 | 路径 | 说明 |
|------|------|------|
| 项目总览 | `CLAUDE.md` | Claude Code 项目总览指引 |
| 后端专属 | `boot/CLAUDE.md` | 后端技术栈、包结构、安全模式、数据库 Schema |
| 前端编码规范 | `ui/ng-plate/AGENTS.md` | Angular/TypeScript 最佳实践（组件、模板、状态管理、无障碍） |
| 项目 README | `README.md` | 功能介绍、快速启动、Docker 部署 |
| 后端 README | `boot/README.md` / `boot/README_CH.md` | 后端部署文档（英文/中文） |

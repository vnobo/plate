# Alex Platform

---

## 环境说明

* **java 17+**
* **gradle 7+**
* **spring boot 3+**
* **redis 7+**
* **rabbitmq 3.9+ 22+**
* **R2DBC**

--------

## 🎁REST API 设计

就是用URL定位资源，用HTTP动词（GET，POST，DELETE，PUT）描述操作。

REST 用来规范应用如何在 HTTP 层与 API 提供方进行数据交互 。REST 描述了 HTTP 层里客户端和服务器端的数据交互规则；客户端通过向服务器端发送
HTTP（s）请求，接收服务器的响应，完成一次 HTTP 交互。这个交互过程中，REST 架构约定两个重要方面就是 HTTP 请求所采用的方法，以及请求的链接。

* **_REST 规范3个原则_**：
    1. 请求 API 的 URL 用来定位资源,进行的操作区分开,这就意味这 URL 不该有任何动词。
    2. 请求 METHOD 对这个资源进行的操作。
    3. 所有错误以HTTP状态码(5xx服务错误,4XX请求错误),以RESPONSE BODY JSON 格式返回.

### 安全认证(Spring security token)

* 获取登录验证码:

> {BASE_HOST}/api/captcha/v1/code

* 登录认证接口:

> {BASE_HOST}/api/oauth2/v1/token

```text
GET /api/oauth2/v1/token
Request Headers
    X-Requested-System : other // 必须的,你需要登录系统
    X-Requested-With: XMLHttpRequest // 必须的
    Authorization: Basic YWRtaW46MTIzNDU2 // 必须的, base64(username+password)
    X-CAPTCHA-TOKEN: xxxx //必须的,登录验证码
```

Response Body

```json
{
  "token": "5dceae5a-62ab-4b20-a022-24c25fa6949f",
  "expires": 14400,
  "lastAccessTime": 1675933482
}
```

* 获取当前登录用户信息:

> {BASE_HOST}/api/auth/oauth2/v1/me

```text
GET /api/auth/oauth2/v1/me
Request Headers
X-Requested-With: XMLHttpRequest
X-Requested-System : other // 必须的,你需要登录系统
X-Auth-Token: 5dceae5a-62ab-4b20-a022-24c25fa6949f
```

Response Body

```json
{
  "username": "admin",
  "enabled": true,
  "roles": [
    "ROLE_GROUP_ADMINISTRATORS",
    "ROLE_ADMINISTRATORS"
  ],
  "name": null,
  "idCard": null,
  "tenants": [
    {
      "code": "0",
      "name": "默认租户",
      "isDefault": true,
      "extend": null
    }
  ],
  "groups": [
    {
      "code": "G1000",
      "name": "administrators"
    }
  ],
  "tenantCode": "0",
  "tenantName": "默认租户",
  "tenantLevel": "NONE"
}
```

* 获取当前CSRF TOKEN:

> **注意**: <br>
> 在浏览器中CSRF TOKEN会保存在 Cookie中.<br>
> 如: Cookie: SESSION=5dceae5a-62ab-4b20-a022-24c25fa6949f; XSRF-TOKEN=8f70dd31-421e-4d7a-b713-b0dad4043ab2<br>
> 当APP,小程序无法使用的Cookie的应用则使用接口获取.<br>
> {BASE_HOST}/api/oauth2/v1/csrf

```text
GET /api/oauth2/v1/csrf
Request Headers
X-Requested-With: XMLHttpRequest
X-Requested-System : other // 必须的,你需要登录系统
X-Auth-Token: 5dceae5a-62ab-4b20-a022-24c25fa6949f
```

Response Body

```json
{
  "token": "8f70dd31-421e-4d7a-b713-b0dad4043ab2",
  "parameterName": "_csrf",
  "headerName": "X-XSRF-TOKEN"
}
```

### 示例 如: 用户

* **API定义规则**:
  > /NGINX代理前缀(api)/服务前缀(auth)/功能前缀(users)/版本号(v1)/增删改查(/search/page/operate/delete)
    ```
     1. 增加用户,    uri: /api/auth/users/v1/operate      接口类型：POST
     2. 删除用户,    uri: /api/auth/users/v1/delete       接口类型：DELETE
     3. 修改用户,    uri: /api/auth/users/v1/operate      接口类型：POST
     4. 查找用户列表, uri: /api/auth/users/v1/search       接口类型：GET
     5. 查找用户分页, uri: /api/auth/users/v1/page         接口类型：GET
    ``` 

* **安全认证请求头Headers**:

| 字段                 | 值                                    | 说明                         |
|--------------------|--------------------------------------|----------------------------|
| X-Auth-Token       | 2c57e428-b37f-4ff5-97a4-02ec3b273d8f | 认证TOKEN,必须的                |
| X-Requested-With   | XMLHttpRequest                       | HTTP BASE 标识,必须的           |
| X-Requested-System | other                                | 必须的,你需要登录系统类型              |
| X-XSRF-TOKEN       | 2c57e428-b37f-4ff5-97a4-02ec3b273d8f | CSRF保护Token保护数据,POST提交是必须的 |

* **POST,DELETE,PUT**:
    * 请求需要CSRF TOKEN:

      ```text
      POST /api/auth/users/v1/operate
      X-Requested-With: XMLHttpRequest
      X-Auth-Token: 2c57e428-b37f-4ff5-97a4-02ec3b273d8f // 必须的 认证TOKEN
      X-XSRF-TOKEN: 2c57e428-b37f-4ff5-97a4-02ec3b273d8f // POST 必须的, CSRF 保护 TOKEN
      ```

    * 请求体BODY必须为JSON (除"上传文件"):

        ```json
        {
            "字段1":"值1",
            "字段2":"值2",
            "字段3":"值3"
        }
        ```

    * 返回体 RESPONSE:

      ```json
      {
         "字段1":"值1",
         "字段2":"值2",
         "字段3":"值3"
      }
      ```

* **GET**:

    * 请求URL QUERY_PARAMS 传参 如:

      ```
      GET /api/auth/users/v1/page?page=0&size=100&sort=id,desc
      X-Auth-Token: null
      X-Requested-With: XMLHttpRequest
      X-XSRF-TOKEN: null
      ```

    * 分页返回体 RESPONSE:

      ```json
      {
          "content":[],
          "pageable":{
              "sort":{"empty":false,"unsorted":false,"sorted":true },
              "offset":0,
              "pageSize":25,
              "pageNumber":0,
              "unpaged":false,
              "paged":true
          },
          "last":true,
          "totalPages":1,
          "totalElements":1,
          "size":25,
          "number":0,
          "sort":{ "empty":false,"unsorted":false,"sorted":true },
          "first":true,"numberOfElements": 1,
          "empty":false
      }
      ```
* **分页参数**:

| 说明  | 字段   | 默认值     | 说明                                                             |
|-----|------|---------|----------------------------------------------------------------|
| 当前页 | page | 0       |
| 请求数 | size | 25      | 默认25,最大每页请求2000                                                |
| 排序  | sort | id,desc | 排序多个:<br>?sort=id,desc&sort=updatedTime,desc<br>ASC 升序 DESC 降序 |

* **错误信息**:
    * HTTP STATUS :
        * HTTP 401 安全认证错误
        * HTTP 407 请求参数错误
        * HTTP 500 系统业务错误
        * HTTP 502,503 服务器未准备就绪
        * HTTP 507 数据库错误
    * 错误体 RESPONSE Body 说明:
    ```json
    {"code":"14xx","msg":"错误简要","errors":"OBJECT(详细错误信息)"}
    ```
    * 错误体 RESPONSE Body 编码对照表:

| 编码   | 简要       | 详细信息             |
|------|----------|------------------|
| 4011 | 认证失败     | token失效,或者账号密码错误 |
| 4071 | 参数空指针    | 请求参数,验证,序列化失败    |
| 4072 | 请求参数验证失败 | 请求参数,验证,序列化失败    |
| 5001 | 数据序列化失败  | 序列化信息失败          |
| 5071 | 数据库超时    | 数据库超时            |
| 5072 | 数据库操作失败  | 数据库操作失败          |

### web ui

<p>Angular app</p>

## Boot

<p>spring boot 3.0.0</p>

## Thanks

-----
![jetbrains](https://resources.jetbrains.com/storage/products/company/brand/logos/jb_beam.png?_ga=2.50027366.1351545062.1678115252-1057454299.1678115252&_gl=1*1difgeb*_ga*MTA1NzQ1NDI5OS4xNjc4MTE1MjUy*_ga_9J976DJZ68*MTY3ODExNTI1MS4xLjEuMTY3ODExNTI4Mi4wLjAuMA..)

* [jetbrains](https://www.jetbrains.com)
* [https://jb.gg/OpenSource](https://jb.gg/OpenSource)
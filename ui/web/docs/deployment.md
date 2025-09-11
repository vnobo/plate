# 部署文档

## 概述

本文档详细描述了 Angular v20 Web 应用程序的部署流程，包括构建配置、服务器设置、CI/CD 集成和生产环境优化。

## 部署架构

### 部署环境

```
部署环境架构
├── 开发环境 (Development)
├── 测试环境 (Testing)
├── 预生产环境 (Staging)
└── 生产环境 (Production)
```

### 部署选项

| 部署方式 | 描述 | 适用场景 |
|----------|------|----------|
| **静态托管** | 纯前端部署 | 简单应用，无 SSR |
| **Node.js 服务器** | 带 SSR 的部署 | 需要 SEO，首屏优化 |
| **容器化部署** | Docker 容器 | 微服务架构 |
| **云平台** | AWS, Azure, GCP | 企业级应用 |
| **CDN 部署** | 全球分发 | 高性能要求 |

## 构建配置

### 生产环境构建

```bash
# 标准生产构建
ng build --configuration=production

# 带统计信息的构建
ng build --configuration=production --stats-json

# 带源映射的构建（调试用）
ng build --configuration=production --source-map=true

# 详细输出构建
ng build --configuration=production --verbose=true
```

### 构建输出

```
dist/web/
├── browser/                    # 客户端代码
│   ├── index.html             # 主 HTML 文件
│   ├── *.js                   # JavaScript 文件
│   ├── *.css                  # CSS 文件
│   ├── assets/                # 静态资源
│   └── *.woff2                # 字体文件
├── server/                    # 服务端代码 (SSR)
│   ├── main.js                # 服务器主文件
│   └── *.js                   # 其他服务器文件
└── *.map                      # 源映射文件 (可选)
```

### 构建优化配置

**文件**: [`angular.json`](angular.json:73-96)

```json
{
  "configurations": {
    "production": {
      "optimization": {
        "scripts": true,
        "styles": {
          "minify": true,
          "inlineCritical": true
        },
        "fonts": true
      },
      "budgets": [
        {
          "type": "initial",
          "maximumWarning": "500kB",
          "maximumError": "1MB"
        },
        {
          "type": "anyComponentStyle",
          "maximumWarning": "4kB",
          "maximumError": "8kB"
        }
      ],
      "outputHashing": "all",
      "serviceWorker": "ngsw-config.json"
    }
  }
}
```

## 静态部署

### Nginx 配置

```nginx
# /etc/nginx/sites-available/angular-app
server {
    listen 80;
    server_name your-domain.com;
    
    # 重定向到 HTTPS
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name your-domain.com;
    
    # SSL 配置
    ssl_certificate /path/to/certificate.crt;
    ssl_certificate_key /path/to/private.key;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;
    
    # 安全头
    add_header X-Frame-Options "SAMEORIGIN" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header X-XSS-Protection "1; mode=block" always;
    add_header Referrer-Policy "no-referrer-when-downgrade" always;
    add_header Content-Security-Policy "default-src 'self' http: https: data: blob: 'unsafe-inline'" always;
    
    # Gzip 压缩
    gzip on;
    gzip_vary on;
    gzip_min_length 1024;
    gzip_proxied expired no-cache no-store private must-revalidate auth;
    gzip_types
        text/plain
        text/css
        text/xml
        text/javascript
        application/javascript
        application/xml+rss
        application/json;
    
    # 根目录
    root /var/www/angular-app/browser;
    index index.html;
    
    # 处理 Angular 路由
    location / {
        try_files $uri $uri/ /index.html;
        
        # 缓存控制
        expires 1h;
        add_header Cache-Control "public, immutable";
    }
    
    # 静态资源缓存
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2|ttf|eot)$ {
        expires 1y;
        add_header Cache-Control "public, immutable";
        access_log off;
    }
    
    # API 代理
    location /api/ {
        proxy_pass http://backend-server:3000;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
    
    # Service Worker
    location /ngsw-worker.js {
        expires 0;
        add_header Cache-Control "no-cache, no-store, must-revalidate";
    }
    
    # Web App Manifest
    location /manifest.webmanifest {
        expires 1h;
        add_header Cache-Control "public";
    }
}
```

### Apache 配置

```apache
# .htaccess 文件
<IfModule mod_rewrite.c>
    RewriteEngine On
    
    # 重定向到 HTTPS
    RewriteCond %{HTTPS} off
    RewriteRule ^(.*)$ https://%{HTTP_HOST}%{REQUEST_URI} [L,R=301]
    
    # Angular 路由处理
    RewriteBase /
    RewriteRule ^index\.html$ - [L]
    RewriteCond %{REQUEST_FILENAME} !-f
    RewriteCond %{REQUEST_FILENAME} !-d
    RewriteRule . /index.html [L]
</IfModule>

# 缓存控制
<IfModule mod_expires.c>
    ExpiresActive On
    
    # 静态资源缓存 1 年
    ExpiresByType image/jpg "access plus 1 year"
    ExpiresByType image/jpeg "access plus 1 year"
    ExpiresByType image/gif "access plus 1 year"
    ExpiresByType image/png "access plus 1 year"
    ExpiresByType text/css "access plus 1 year"
    ExpiresByType application/javascript "access plus 1 year"
    ExpiresByType application/json "access plus 1 year"
    
    # HTML 文件缓存 1 小时
    ExpiresByType text/html "access plus 1 hour"
</IfModule>

# 压缩
<IfModule mod_deflate.c>
    AddOutputFilterByType DEFLATE text/html text/plain text/css text/javascript application/javascript application/json
</IfModule>
```

## SSR 部署

### Node.js 服务器部署

#### 服务器脚本

**文件**: [`server.ts`](server.ts:1)

```typescript
import 'zone.js/node';
import { APP_BASE_HREF } from '@angular/common';
import { ngExpressEngine } from '@nguniversal/express-engine';
import express from 'express';
import { existsSync } from 'node:fs';
import { join } from 'node:path';
import { AppServerModule } from './src/main.server';

// Express server
export function app(): express.Express {
  const server = express();
  const distFolder = join(process.cwd(), 'dist/web/browser');
  const indexHtml = existsSync(join(distFolder, 'index.original.html')) ? 'index.original.html' : 'index';

  // Our Universal express-engine (found @ https://github.com/angular/universal/tree/main/modules/express-engine)
  server.engine('html', ngExpressEngine({
    bootstrap: AppServerModule
  }));

  server.set('view engine', 'html');
  server.set('views', distFolder);

  // Serve static files from /browser
  server.get('*.*', express.static(distFolder, {
    maxAge: '1y',
    setHeaders: (res, path) => {
      if (path.includes('ngsw-worker.js')) {
        res.setHeader('Cache-Control', 'no-cache, no-store, must-revalidate');
      }
    }
  }));

  // All regular routes use the Universal engine
  server.get('*', (req, res) => {
    res.render(indexHtml, { req, providers: [{ provide: APP_BASE_HREF, useValue: req.baseUrl }] });
  });

  return server;
}

function run(): void {
  const port = process.env['PORT'] || 4000;

  // Start up the Node server
  const server = app();
  server.listen(port, () => {
    console.log(`Node Express server listening on http://localhost:${port}`);
  });
}

// Webpack will replace 'require' with '__webpack_require__'
// '__non_webpack_require__' is a proxy to Node 'require'
// The below code is to ensure that the server is run only when not requiring the bundle.
declare const __non_webpack_require__: NodeRequire;
const mainModule = __non_webpack_require__.main;
const moduleFilename = mainModule && mainModule.filename || '';
if (moduleFilename === __filename || moduleFilename.includes('iisnode')) {
  run();
}

export * from './src/main.server';
```

#### PM2 进程管理

**文件**: [`ecosystem.config.js`](ecosystem.config.js:1)

```javascript
module.exports = {
  apps: [{
    name: 'angular-web-app',
    script: './dist/web/server/main.js',
    instances: 'max',
    exec_mode: 'cluster',
    env: {
      NODE_ENV: 'production',
      PORT: 4000
    },
    env_development: {
      NODE_ENV: 'development',
      PORT: 4200
    },
    error_file: './logs/err.log',
    out_file: './logs/out.log',
    log_file: './logs/combined.log',
    time: true,
    max_memory_restart: '1G',
    node_args: '--max-old-space-size=4096'
  }]
};
```

### Docker 部署

#### Dockerfile

**文件**: [`Dockerfile`](Dockerfile:1)

```dockerfile
# 构建阶段
FROM node:18-alpine AS builder

WORKDIR /app

# 复制包文件
COPY package*.json ./
RUN npm ci --only=production

# 复制源代码
COPY . .

# 构建应用
RUN npm run build:ssr

# 生产阶段
FROM node:18-alpine

WORKDIR /app

# 安装 PM2
RUN npm install -g pm2

# 复制构建产物
COPY --from=builder /app/dist ./dist
COPY --from=builder /app/ecosystem.config.js ./
COPY --from=builder /app/package*.json ./

# 安装生产依赖
RUN npm ci --only=production

# 创建非 root 用户
RUN addgroup -g 1001 -S nodejs
RUN adduser -S angular -u 1001

# 更改权限
RUN chown -R angular:nodejs /app
USER angular

# 暴露端口
EXPOSE 4000

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:4000/health || exit 1

# 启动应用
CMD ["pm2-runtime", "start", "ecosystem.config.js"]
```

#### Docker Compose

**文件**: [`docker-compose.yml`](docker-compose.yml:1)

```yaml
version: '3.8'

services:
  angular-app:
    build:
      context: .
      dockerfile: Dockerfile
    ports:
      - "4000:4000"
    environment:
      - NODE_ENV=production
      - PORT=4000
    volumes:
      - ./logs:/app/logs
    restart: unless-stopped
    networks:
      - app-network
    depends_on:
      - redis
      - postgres

  nginx:
    image: nginx:alpine
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./nginx.conf:/etc/nginx/nginx.conf
      - ./ssl:/etc/nginx/ssl
    depends_on:
      - angular-app
    networks:
      - app-network
    restart: unless-stopped

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    volumes:
      - redis-data:/data
    networks:
      - app-network
    restart: unless-stopped

  postgres:
    image: postgres:15-alpine
    environment:
      POSTGRES_DB: angular_app
      POSTGRES_USER: angular_user
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    volumes:
      - postgres-data:/var/lib/postgresql/data
    networks:
      - app-network
    restart: unless-stopped

networks:
  app-network:
    driver: bridge

volumes:
  redis-data:
  postgres-data:
```

## 云平台部署

### AWS 部署

#### AWS S3 + CloudFront (静态部署)

```bash
# 安装 AWS CLI
pip install awscli

# 配置 AWS 凭证
aws configure

# 创建 S3 存储桶
aws s3 mb s3://your-angular-app-bucket

# 启用静态网站托管
aws s3 website s3://your-angular-app-bucket --index-document index.html --error-document index.html

# 上传构建文件
aws s3 sync dist/web/browser s3://your-angular-app-bucket --delete

# 设置存储桶策略
cat > bucket-policy.json << EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "PublicReadGetObject",
      "Effect": "Allow",
      "Principal": "*",
      "Action": "s3:GetObject",
      "Resource": "arn:aws:s3:::your-angular-app-bucket/*"
    }
  ]
}
EOF

aws s3api put-bucket-policy --bucket your-angular-app-bucket --policy file://bucket-policy.json

# 创建 CloudFront 分发
aws cloudfront create-distribution --origin-domain-name your-angular-app-bucket.s3.amazonaws.com
```

#### AWS ECS (容器部署)

```json
{
  "family": "angular-app-task",
  "networkMode": "awsvpc",
  "requiresCompatibilities": ["FARGATE"],
  "cpu": "512",
  "memory": "1024",
  "executionRoleArn": "arn:aws:iam::account:role/ecsTaskExecutionRole",
  "containerDefinitions": [
    {
      "name": "angular-app",
      "image": "your-registry/angular-app:latest",
      "portMappings": [
        {
          "containerPort": 4000,
          "protocol": "tcp"
        }
      ],
      "environment": [
        {
          "name": "NODE_ENV",
          "value": "production"
        }
      ],
      "logConfiguration": {
        "logDriver": "awslogs",
        "options": {
          "awslogs-group": "/ecs/angular-app",
          "awslogs-region": "us-west-2",
          "awslogs-stream-prefix": "ecs"
        }
      }
    }
  ]
}
```

### Azure 部署

#### Azure Static Web Apps

```yaml
# .github/workflows/azure-static-web-apps.yml
name: Deploy to Azure Static Web Apps

on:
  push:
    branches: [ main ]
  pull_request:
    types: [opened, synchronize, reopened, closed]
    branches: [ main ]

jobs:
  build_and_deploy_job:
    if: github.event_name == 'push' || (github.event.pull_request.head.repo.full_name == github.repository)
    runs-on: ubuntu-latest
    name: Build and Deploy Job
    steps:
      - uses: actions/checkout@v3
        with:
          submodules: true
      
      - name: Build And Deploy
        id: builddeploy
        uses: Azure/static-web-apps-deploy@v1
        with:
          azure_static_web_apps_api_token: ${{ secrets.AZURE_STATIC_WEB_APPS_API_TOKEN }}
          repo_token: ${{ secrets.GITHUB_TOKEN }}
          action: "upload"
          app_location: "/" 
          api_location: "" 
          output_location: "dist/web/browser"
```

### Google Cloud Platform 部署

#### Google App Engine

```yaml
# app.yaml
runtime: nodejs18
service: angular-app

instance_class: F2

automatic_scaling:
  min_instances: 1
  max_instances: 10
  target_cpu_utilization: 0.65

env_variables:
  NODE_ENV: production
  PORT: 8080

handlers:
- url: /(.*\.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2|ttf|eot))
  static_files: dist/web/browser/\1
  upload: dist/web/browser/.*\.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2|ttf|eot)
  expiration: "1d"

- url: /.*
  script: auto
  secure: always
```

## CI/CD 集成

### GitHub Actions

**文件**: [`.github/workflows/deploy.yml`](.github/workflows/deploy.yml:1)

```yaml
name: Deploy to Production

on:
  push:
    branches: [ main ]
  workflow_dispatch:

env:
  NODE_VERSION: '18'
  APP_NAME: 'angular-web-app'

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Setup Node.js
        uses: actions/setup-node@v3
        with:
          node-version: ${{ env.NODE_VERSION }}
          cache: 'npm'
      
      - name: Install dependencies
        run: npm ci
      
      - name: Run tests
        run: npm run test:ci
      
      - name: Upload coverage reports
        uses: codecov/codecov-action@v3
        with:
          file: ./coverage/lcov.info

  build:
    needs: test
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Setup Node.js
        uses: actions/setup-node@v3
        with:
          node-version: ${{ env.NODE_VERSION }}
          cache: 'npm'
      
      - name: Install dependencies
        run: npm ci
      
      - name: Build application
        run: npm run build:ssr
      
      - name: Upload build artifacts
        uses: actions/upload-artifact@v3
        with:
          name: build-files
          path: dist/

  deploy:
    needs: build
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Download build artifacts
        uses: actions/download-artifact@v3
        with:
          name: build-files
          path: dist/
      
      - name: Deploy to server
        uses: appleboy/ssh-action@v0.1.5
        with:
          host: ${{ secrets.HOST }}
          username: ${{ secrets.USERNAME }}
          key: ${{ secrets.SSH_KEY }}
          script: |
            # 停止当前服务
            pm2 stop ${{ env.APP_NAME }} || true
            
            # 备份当前版本
            cp -r /var/www/angular-app /var/www/angular-app-backup-$(date +%Y%m%d-%H%M%S)
            
            # 复制新构建文件
            rm -rf /var/www/angular-app/*
            cp -r dist/* /var/www/angular-app/
            
            # 安装生产依赖
            cd /var/www/angular-app
            npm ci --only=production
            
            # 重启服务
            pm2 start ecosystem.config.js --env production
            
            # 健康检查
            sleep 10
            curl -f http://localhost:4000/health || exit 1
```

### GitLab CI/CD

**文件**: [`.gitlab-ci.yml`](.gitlab-ci.yml:1)

```yaml
stages:
  - test
  - build
  - deploy

variables:
  NODE_VERSION: "18"
  APP_NAME: "angular-web-app"

cache:
  paths:
    - node_modules/

test:
  stage: test
  image: node:${NODE_VERSION}
  script:
    - npm ci
    - npm run test:ci
    - npm run lint
  coverage: '/Lines\s*:\s*(\d+\.\d+)%/'
  artifacts:
    reports:
      coverage_report:
        coverage_format: cobertura
        path: coverage/cobertura-coverage.xml

build:
  stage: build
  image: node:${NODE_VERSION}
  script:
    - npm ci
    - npm run build:ssr
  artifacts:
    paths:
      - dist/
    expire_in: 1 hour

deploy production:
  stage: deploy
  image: alpine:latest
  before_script:
    - apk add --no-cache openssh-client rsync
  script:
    - eval $(ssh-agent -s)
    - echo "$SSH_PRIVATE_KEY" | tr -d '\r' | ssh-add -
    - mkdir -p ~/.ssh
    - chmod 700 ~/.ssh
    - ssh-keyscan -H $DEPLOYMENT_SERVER >> ~/.ssh/known_hosts
    - |
      rsync -avz --delete \
        --exclude='node_modules' \
        --exclude='.git' \
        dist/ $DEPLOYMENT_USER@$DEPLOYMENT_SERVER:/var/www/angular-app/
    - |
      ssh $DEPLOYMENT_USER@$DEPLOYMENT_SERVER << 'ENDSSH'
        cd /var/www/angular-app
        npm ci --only=production
        pm2 reload ecosystem.config.js --env production
      ENDSSH
  only:
    - main
  when: manual
```

## 环境配置

### 环境变量

```bash
# 生产环境变量
NODE_ENV=production
PORT=4000
API_URL=https://api.yourdomain.com
WEBSOCKET_URL=wss://ws.yourdomain.com
REDIS_URL=redis://localhost:6379
DATABASE_URL=postgresql://user:pass@localhost:5432/dbname
JWT_SECRET=your-jwt-secret
ENCRYPTION_KEY=your-encryption-key
```

### 环境配置文件

**文件**: [`src/envs/env.ts`](src/envs/env.ts:1)

```typescript
export const environment = {
  production: true,
  apiUrl: process.env['API_URL'] || 'https://api.yourdomain.com',
  websocketUrl: process.env['WEBSOCKET_URL'] || 'wss://ws.yourdomain.com',
  appVersion: process.env['npm_package_version'] || '1.0.0',
  enableServiceWorker: true,
  enableAnalytics: true,
  enableErrorReporting: true,
  features: {
    darkMode: true,
    notifications: true,
    offlineMode: true
  }
};
```

## 监控和日志

### 应用监控

```typescript
// 健康检查端点
app.get('/health', (req, res) => {
  const health = {
    status: 'healthy',
    timestamp: new Date().toISOString(),
    uptime: process.uptime(),
    memory: process.memoryUsage(),
    version: process.env.npm_package_version
  };
  
  res.status(200).json(health);
});

// 性能监控
app.use((req, res, next) => {
  const start = Date.now();
  
  res.on('finish', () => {
    const duration = Date.now() - start;
    console.log(`${req.method} ${req.url} - ${duration}ms`);
  });
  
  next();
});
```

### 日志配置

```typescript
// winston 日志配置
import winston from 'winston';

const logger = winston.createLogger({
  level: 'info',
  format: winston.format.combine(
    winston.format.timestamp(),
    winston.format.errors({ stack: true }),
    winston.format.json()
  ),
  defaultMeta: { service: 'angular-app' },
  transports: [
    new winston.transports.File({ filename: 'logs/error.log', level: 'error' }),
    new winston.transports.File({ filename: 'logs/combined.log' }),
    new winston.transports.Console({
      format: winston.format.simple()
    })
  ]
});
```

## 性能优化

### 构建优化

```bash
# 分析包大小
npm run build:stats
npx webpack-bundle-analyzer dist/web/browser/stats.json

# 优化建议
# 1. 使用懒加载
# 2. 优化第三方库
# 3. 启用 Gzip 压缩
# 4. 使用 CDN
# 5. 优化图片资源
```

### 运行时优化

```typescript
// 启用生产模式
enableProdMode();

// 优化变更检测
@Component({
  changeDetection: ChangeDetectionStrategy.OnPush
})

// 使用 trackBy 函数
*ngFor="let item of items; trackBy: trackByFn"

// 虚拟滚动
<cdk-virtual-scroll-viewport itemSize="50">
  <div *cdkVirtualFor="let item of items">{{item}}</div>
</cdk-virtual-scroll-viewport>
```

## 安全加固

### 安全头配置

```nginx
# 安全头配置
add_header X-Frame-Options "SAMEORIGIN" always;
add_header X-Content-Type-Options "nosniff" always;
add_header X-XSS-Protection "1; mode=block" always;
add_header Referrer-Policy "no-referrer-when-downgrade" always;
add_header Content-Security-Policy "default-src 'self'; script-src 'self' 'unsafe-inline' 'unsafe-eval'; style-src 'self' 'unsafe-inline';" always;
add_header Strict-Transport-Security "max-age=31536000; includeSubDomains" always;
```

### 环境安全

```bash
# 敏感信息加密
export JWT_SECRET=$(openssl rand -base64 32)
export ENCRYPTION_KEY=$(openssl rand -base64 32)

# 文件权限
chmod 600 .env
chmod 755 /var/www/angular-app
```

## 回滚策略

### 快速回滚

```bash
#!/bin/bash
# rollback.sh

APP_NAME="angular-web-app"
BACKUP_DIR="/var/www/backups"
CURRENT_DIR="/var/www/angular-app"

# 获取最新备份
LATEST_BACKUP=$(ls -t $BACKUP_DIR | head -1)

if [ -z "$LATEST_BACKUP" ]; then
    echo "没有找到备份文件"
    exit 1
fi

# 停止当前服务
pm2 stop $APP_NAME

# 恢复备份
rm -rf $CURRENT_DIR
cp -r "$BACKUP_DIR/$LATEST_BACKUP" $CURRENT_DIR

# 重启服务
pm2 start $APP_NAME

echo "回滚完成: $LATEST_BACKUP"
```

## 故障排除

### 常见问题

1. **构建失败**

   ```bash
   # 清理缓存
   rm -rf node_modules package-lock.json
   npm cache clean --force
   npm install
   ```

2. **内存不足**

   ```bash
   # 增加 Node.js 内存限制
   export NODE_OPTIONS="--max-old-space-size=4096"
   ng build --configuration=production
   ```

3. **Service Worker 问题**

   ```bash
   # 清除 Service Worker 缓存
   curl -X DELETE http://localhost:4000/ngsw-cache
   ```

---

**文档版本**: 1.0.0  
**最后更新**: 2025年

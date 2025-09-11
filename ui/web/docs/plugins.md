# 插件系统文档

## 概述

Angular v20 Web 应用程序包含一个强大的插件系统，提供可重用的 UI 组件和服务。插件系统设计为模块化、可配置且易于使用。

## 插件架构

### 设计原则

1. **模块化** - 每个插件都是独立的模块
2. **可配置** - 支持自定义配置选项
3. **类型安全** - 完整的 TypeScript 类型支持
4. **可测试** - 易于单元测试
5. **可扩展** - 支持自定义扩展

### 插件结构

```
src/app/plugins/
├── alerts/           # 警告提示插件
├── modals/           # 模态框插件
└── toasts/           # 消息提示插件
```

## 警告插件 (Alerts)

**文件**: [`src/app/plugins/alerts/alerts.ts`](src/app/plugins/alerts/alerts.ts:1)

### 功能特性

- 多种警告类型（成功、错误、警告、信息）
- 可配置的位置和持续时间
- 自动关闭功能
- 手动关闭支持
- 动画效果

### API 接口

```typescript
class Alerts {
  // 显示成功警告
  success(message: string, options?: AlertOptions): void
  
  // 显示错误警告
  error(message: string, options?: AlertOptions): void
  
  // 显示警告信息
  warning(message: string, options?: AlertOptions): void
  
  // 显示信息警告
  info(message: string, options?: AlertOptions): void
}
```

### 配置选项

```typescript
interface AlertOptions {
  duration?: number;        // 显示时长（毫秒），默认 3000
  position?: AlertPosition; // 显示位置，默认 'top-right'
  closable?: boolean;       // 是否可手动关闭，默认 true
  autoClose?: boolean;      // 是否自动关闭，默认 true
  animation?: boolean;      // 是否启用动画，默认 true
  className?: string;       // 自定义 CSS 类名
}
```

### 位置选项

```typescript
type AlertPosition = 
  | 'top-left' 
  | 'top-center' 
  | 'top-right'
  | 'bottom-left' 
  | 'bottom-center' 
  | 'bottom-right'
  | 'center';
```

### 使用示例

#### 基本使用

```typescript
import { Alerts } from '@app/plugins';

export class UserComponent {
  private readonly alerts = inject(Alerts);
  
  onSaveSuccess() {
    this.alerts.success('用户保存成功！');
  }
  
  onSaveError(error: string) {
    this.alerts.error(`保存失败: ${error}`);
  }
}
```

#### 高级配置

```typescript
// 自定义配置
this.alerts.warning('请注意，此操作不可撤销！', {
  duration: 5000,
  position: 'center',
  closable: true,
  autoClose: false,
  className: 'custom-alert'
});
```

### 样式定制

```scss
// 自定义警告样式
.custom-alert {
  .alert-icon {
    color: #ff6b6b;
  }
  
  .alert-message {
    font-size: 16px;
    font-weight: 500;
  }
}
```

## 消息提示插件 (Toasts)

**文件**: [`src/app/plugins/toasts/toasts.ts`](src/app/plugins/toasts/toasts.ts:1)

### 功能特性

- 多种消息类型（成功、错误、警告、信息）
- 支持标题和内容
- 自动堆叠显示
- 进度条显示剩余时间
- 触摸滑动关闭

### API 接口

```typescript
class Toasts {
  // 显示成功消息
  success(message: string, title?: string, options?: ToastOptions): void
  
  // 显示错误消息
  error(message: string, title?: string, options?: ToastOptions): void
  
  // 显示警告消息
  warning(message: string, title?: string, options?: ToastOptions): void
  
  // 显示信息消息
  info(message: string, title?: string, options?: ToastOptions): void
}
```

### 配置选项

```typescript
interface ToastOptions {
  duration?: number;        // 显示时长（毫秒），默认 5000
  position?: ToastPosition; // 显示位置，默认 'top-right'
  closable?: boolean;       // 是否可关闭，默认 true
  pauseOnHover?: boolean;   // 悬停时暂停，默认 true
  progressBar?: boolean;    // 显示进度条，默认 true
  animation?: boolean;      // 启用动画，默认 true
  maxToasts?: number;       // 最大显示数量，默认 5
  newestOnTop?: boolean;    // 新消息置顶，默认 true
}
```

### 使用示例

#### 基本使用

```typescript
import { Toasts } from '@app/plugins';

export class DataComponent {
  private readonly toasts = inject(Toasts);
  
  onDataLoaded() {
    this.toasts.success('数据加载完成', '成功');
  }
  
  onDataError(error: string) {
    this.toasts.error(error, '数据加载失败');
  }
}
```

#### 带标题的消息

```typescript
// 成功消息
this.toasts.success('文件已成功上传到服务器', '上传完成');

// 错误消息
this.toasts.error('网络连接超时，请检查网络设置', '连接失败');

// 警告消息
this.toasts.warning('磁盘空间即将不足，请及时清理', '存储警告');

// 信息消息
this.toasts.info('新版本可用，建议及时更新', '更新提示');
```

#### 高级配置

```typescript
this.toasts.info('系统将在5分钟后进行维护', '维护通知', {
  duration: 10000,
  position: 'bottom-right',
  pauseOnHover: true,
  progressBar: true,
  maxToasts: 3
});
```

### 消息服务 (MessageService)

**文件**: [`src/app/plugins/toasts/toasts.ts`](src/app/plugins/toasts/toasts.ts:1)

提供基于 Observable 的消息服务。

#### API 接口

```typescript
class MessageService {
  // 发送消息
  send(message: ToastMessage): void
  
  // 清除所有消息
  clear(): void
  
  // 监听消息
  onMessage(): Observable<ToastMessage>
}
```

#### 使用示例

```typescript
import { MessageService } from '@app/plugins';

export class AppComponent {
  private readonly messageService = inject(MessageService);
  
  ngOnInit() {
    // 监听消息
    this.messageService.onMessage().subscribe(message => {
      console.log('收到消息:', message);
    });
  }
  
  sendCustomMessage() {
    this.messageService.send({
      type: 'info',
      title: '系统消息',
      message: '这是一条自定义消息',
      duration: 3000
    });
  }
}
```

## 模态框插件 (Modals)

**文件**: [`src/app/plugins/modals/modals.ts`](src/app/plugins/modals/modals.ts:1)

### 功能特性

- 动态组件加载
- 多种尺寸支持
- 背景遮罩配置
- 键盘导航支持
- 返回值处理
- 堆叠显示支持

### API 接口

```typescript
class ModalsService {
  // 打开模态框
  open<T>(component: Type<T>, options?: ModalOptions): ModalRef<T>
  
  // 关闭所有模态框
  closeAll(): void
  
  // 获取活动模态框数量
  getActiveModalsCount(): number
}
```

### 配置选项

```typescript
interface ModalOptions {
  size?: ModalSize;              // 模态框尺寸，默认 'md'
  backdrop?: boolean | 'static'; // 背景遮罩，默认 true
  keyboard?: boolean;            // 键盘导航，默认 true
  centered?: boolean;            // 垂直居中，默认 true
  scrollable?: boolean;          // 可滚动内容，默认 false
  data?: any;                    // 传递给组件的数据
  className?: string;            // 自定义 CSS 类名
  closeButton?: boolean;         // 显示关闭按钮，默认 true
  title?: string;                // 模态框标题
}
```

### 尺寸选项

```typescript
type ModalSize = 'sm' | 'md' | 'lg' | 'xl' | 'fullscreen';
```

### 模态框引用 (ModalRef)

```typescript
interface ModalRef<T> {
  // 组件实例
  componentInstance: T;
  
  // 模态框配置
  config: ModalOptions;
  
  // 关闭模态框
  close(result?: any): void;
  
  // 销毁模态框
  destroy(): void;
  
  // 监听关闭事件
  onClose: Observable<any>;
  
  // 监听销毁事件
  onDestroy: Observable<void>;
}
```

### 使用示例

#### 创建模态框组件

```typescript
// user-edit-modal.ts
import { Component, Inject, inject } from '@angular/core';
import { ModalRef, MODAL_DATA } from '@app/plugins';

@Component({
  selector: 'app-user-edit-modal',
  template: `
    <div class="modal-header">
      <h5 class="modal-title">{{ title }}</h5>
      <button type="button" class="btn-close" (click)="close()"></button>
    </div>
    <div class="modal-body">
      <form [formGroup]="userForm">
        <!-- 表单内容 -->
      </form>
    </div>
    <div class="modal-footer">
      <button type="button" class="btn btn-secondary" (click)="close()">
        取消
      </button>
      <button type="button" class="btn btn-primary" (click)="save()">
        保存
      </button>
    </div>
  `
})
export class UserEditModal {
  private readonly modalRef = inject(ModalRef);
  
  @Inject(MODAL_DATA) data: any;
  title = '编辑用户';
  userForm: FormGroup;
  
  ngOnInit() {
    if (this.data.user) {
      this.userForm.patchValue(this.data.user);
    }
  }
  
  close() {
    this.modalRef.close();
  }
  
  save() {
    if (this.userForm.valid) {
      this.modalRef.close(this.userForm.value);
    }
  }
}
```

#### 打开模态框

```typescript
import { ModalsService } from '@app/plugins';

export class UserListComponent {
  private readonly modals = inject(ModalsService);
  
  editUser(user: User) {
    const modalRef = this.modals.open(UserEditModal, {
      size: 'lg',
      title: '编辑用户',
      data: { user },
      backdrop: true,
      keyboard: true
    });
    
    // 监听关闭事件
    modalRef.onClose.subscribe(result => {
      if (result) {
        // 处理保存结果
        this.updateUser(result);
        this.toasts.success('用户更新成功');
      }
    });
  }
}
```

#### 不同尺寸的模态框

```typescript
// 小尺寸
this.modals.open(ConfirmModal, { size: 'sm' });

// 中等尺寸（默认）
this.modals.open(EditModal, { size: 'md' });

// 大尺寸
this.modals.open(DetailModal, { size: 'lg' });

// 超大尺寸
this.modals.open(FullModal, { size: 'xl' });

// 全屏
this.modals.open(FullscreenModal, { size: 'fullscreen' });
```

### 高级用法

#### 堆叠模态框

```typescript
// 第一个模态框
const modal1 = this.modals.open(FirstModal);

// 在第一个模态框中打开第二个模态框
modal1.componentInstance.openSecondModal = () => {
  const modal2 = this.modals.open(SecondModal);
  
  modal2.onClose.subscribe(result => {
    // 处理第二个模态框的结果
    modal1.close(result);
  });
};
```

#### 静态背景

```typescript
// 防止点击背景关闭模态框
this.modals.open(ImportantModal, {
  backdrop: 'static',
  keyboard: false, // 禁用 ESC 键关闭
  closeButton: false // 隐藏关闭按钮
});
```

## 插件集成

### 在组件中使用

```typescript
import { Component, inject } from '@angular/core';
import { Alerts, Toasts, ModalsService } from '@app/plugins';

@Component({
  selector: 'app-example',
  template: `...`
})
export class ExampleComponent {
  private readonly alerts = inject(Alerts);
  private readonly toasts = inject(Toasts);
  private readonly modals = inject(ModalsService);
  
  showAlert() {
    this.alerts.success('操作成功！');
  }
  
  showToast() {
    this.toasts.info('这是一条消息', '提示');
  }
  
  showModal() {
    this.modals.open(MyModalComponent);
  }
}
```

### 在服务中使用

```typescript
import { Injectable, inject } from '@angular/core';
import { Alerts, Toasts } from '@app/plugins';

@Injectable({
  providedIn: 'root'
})
export class DataService {
  private readonly alerts = inject(Alerts);
  private readonly toasts = inject(Toasts);
  
  async loadData() {
    try {
      const data = await this.fetchData();
      this.toasts.success('数据加载成功', '完成');
      return data;
    } catch (error) {
      this.alerts.error('数据加载失败，请稍后重试');
      throw error;
    }
  }
}
```

## 自定义插件开发

### 创建自定义插件

```typescript
// notification.plugin.ts
import { Injectable, inject } from '@angular/core';
import { Alerts } from '@app/plugins';

@Injectable({
  providedIn: 'root'
})
export class NotificationPlugin {
  private readonly alerts = inject(Alerts);
  
  showSystemNotification(message: string, type: 'info' | 'warning' = 'info') {
    const prefix = type === 'warning' ? '⚠️ 系统警告' : 'ℹ️ 系统通知';
    this.alerts.info(`${prefix}: ${message}`, {
      duration: type === 'warning' ? 10000 : 5000,
      position: 'top-center'
    });
  }
  
  showBusinessNotification(message: string, action?: string) {
    const fullMessage = action ? `${message} (${action})` : message;
    this.alerts.success(`📊 业务通知: ${fullMessage}`);
  }
}
```

### 插件配置

```typescript
// 在 app.config.ts 中注册插件
export const appConfig: ApplicationConfig = {
  providers: [
    // ... 其他配置
    NotificationPlugin,
    // 其他自定义插件
  ]
};
```

## 样式定制

### 主题变量

```scss
// 插件主题变量
:root {
  // 警告插件
  --alert-success-bg: #d4edda;
  --alert-success-color: #155724;
  --alert-error-bg: #f8d7da;
  --alert-error-color: #721c24;
  --alert-warning-bg: #fff3cd;
  --alert-warning-color: #856404;
  --alert-info-bg: #d1ecf1;
  --alert-info-color: #0c5460;
  
  // 消息提示插件
  --toast-success-bg: #28a745;
  --toast-error-bg: #dc3545;
  --toast-warning-bg: #ffc107;
  --toast-info-bg: #17a2b8;
  
  // 模态框插件
  --modal-backdrop-bg: rgba(0, 0, 0, 0.5);
  --modal-border-radius: 0.5rem;
  --modal-box-shadow: 0 0.5rem 1rem rgba(0, 0, 0, 0.15);
}
```

### 自定义样式

```scss
// 自定义警告样式
.custom-alert {
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  
  .alert-icon {
    font-size: 24px;
    margin-right: 12px;
  }
  
  .alert-content {
    flex: 1;
  }
  
  .alert-close {
    opacity: 0.7;
    transition: opacity 0.2s;
    
    &:hover {
      opacity: 1;
    }
  }
}

// 自定义消息提示样式
.custom-toast {
  border-radius: 6px;
  backdrop-filter: blur(10px);
  background-color: rgba(255, 255, 255, 0.9);
  
  .toast-progress {
    height: 3px;
    background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
  }
}

// 自定义模态框样式
.custom-modal {
  .modal-content {
    border: none;
    border-radius: 12px;
    overflow: hidden;
  }
  
  .modal-header {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    border-bottom: none;
  }
}
```

## 性能优化

### 延迟加载

```typescript
// 延迟加载插件模块
const loadAlertsPlugin = () => import('@app/plugins/alerts');
const loadToastsPlugin = () => import('@app/plugins/toasts');
const loadModalsPlugin = () => import('@app/plugins/modals');
```

### 内存管理

```typescript
// 及时清理订阅
export class Component implements OnDestroy {
  private destroy$ = new Subject<void>();
  
  ngOnInit() {
    this.messageService.onMessage()
      .pipe(takeUntil(this.destroy$))
      .subscribe(message => {
        // 处理消息
      });
  }
  
  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }
}
```

## 测试

### 单元测试示例

```typescript
// alerts.spec.ts
describe('Alerts', () => {
  let service: Alerts;
  
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [Alerts]
    });
    service = TestBed.inject(Alerts);
  });
  
  it('should display success alert', () => {
    spyOn(service, 'success');
    service.success('Test message');
    expect(service.success).toHaveBeenCalledWith('Test message');
  });
});
```

## 故障排除

### 常见问题

1. **插件不显示**
   - 检查是否正确导入插件模块
   - 确认样式文件已加载
   - 检查 z-index 层级

2. **样式冲突**
   - 使用自定义类名避免冲突
   - 检查 CSS 特异性
   - 使用 !important 谨慎

3. **内存泄漏**
   - 及时清理订阅
   - 正确销毁组件
   - 避免循环引用

---

**文档版本**: 1.0.0  
**最后更新**: 2025年

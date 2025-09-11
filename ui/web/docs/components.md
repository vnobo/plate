# 组件文档

## 概述

本文档详细描述了 Angular v20 Web 应用程序中所有组件的使用方法、API 和最佳实践。

## 组件架构

### 组件类型

```
组件分类
├── 页面组件 (Page Components)
│   ├── 认证页面
│   ├── 主页页面
│   ├── 仪表板页面
│   └── 错误页面
├── 布局组件 (Layout Components)
│   ├── 基础布局
│   └── 导航组件
├── 业务组件 (Business Components)
│   ├── 用户管理组件
│   └── 表单组件
└── 通用组件 (Shared Components)
    ├── UI 组件
    └── 功能组件
```

### 组件设计原则

1. **单一职责** - 每个组件只负责一个功能
2. **可复用性** - 设计可复用的组件
3. **可测试性** - 易于测试的组件结构
4. **性能优化** - 使用 OnPush 和信号优化
5. **类型安全** - 完整的 TypeScript 类型支持

## 页面组件

### 认证组件

#### LoginComponent

**文件**: [`src/app/pages/passport/login/login.ts`](src/app/pages/passport/login/login.ts:1)

登录页面组件，处理用户认证。

##### API 接口

```typescript
@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule, RouterModule],
  templateUrl: './login.html',
  styleUrls: ['./login.scss']
})
export class LoginComponent {
  loginForm: FormGroup;
  loading = signal(false);
  error = signal<string | null>(null);
  
  onSubmit(): void;
  navigateToRegister(): void;
  navigateToForgotPassword(): void;
}
```

##### 使用示例

```html
<!-- 登录表单 -->
<form [formGroup]="loginForm" (ngSubmit)="onSubmit()">
  <div class="form-group">
    <label for="email">邮箱</label>
    <input 
      type="email" 
      id="email" 
      formControlName="email"
      placeholder="请输入邮箱">
    <div *ngIf="loginForm.get('email')?.errors" class="error">
      邮箱格式不正确
    </div>
  </div>
  
  <div class="form-group">
    <label for="password">密码</label>
    <input 
      type="password" 
      id="password" 
      formControlName="password"
      placeholder="请输入密码">
  </div>
  
  <button 
    type="submit" 
    [disabled]="loginForm.invalid || loading()"
    class="btn btn-primary">
    <span *ngIf="loading()">登录中...</span>
    <span *ngIf="!loading()">登录</span>
  </button>
</form>
```

##### 表单验证

```typescript
// 登录表单验证规则
private buildForm(): void {
  this.loginForm = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]]
  });
}
```

#### RegisterComponent

用户注册组件（待实现）

### 主页组件

#### HomeComponent

**文件**: [`src/app/pages/home/home.ts`](src/app/pages/home/home.ts:1)

主页布局组件，提供主要的应用导航。

##### API 接口

```typescript
@Component({
  selector: 'app-home',
  standalone: true,
  imports: [RouterModule, CommonModule],
  template: `
    <div class="home-container">
      <app-sidebar></app-sidebar>
      <main class="main-content">
        <router-outlet></router-outlet>
      </main>
    </div>
  `
})
export class HomeComponent {
  // 主页布局逻辑
}
```

#### DashboardComponent

仪表板组件，显示系统概览信息。

##### API 接口

```typescript
@Component({
  selector: 'app-dashboard',
  standalone: true,
  template: `
    <div class="dashboard">
      <h1>欢迎使用 Angular v20 应用</h1>
      <div class="stats-grid">
        <div class="stat-card" *ngFor="let stat of stats()">
          <h3>{{ stat.title }}</h3>
          <p class="stat-value">{{ stat.value }}</p>
        </div>
      </div>
    </div>
  `
})
export class DashboardComponent {
  stats = signal<Stat[]>([
    { title: '用户总数', value: '1,234' },
    { title: '活跃用户', value: '567' },
    { title: '今日访问', value: '890' }
  ]);
}

interface Stat {
  title: string;
  value: string;
}
```

### 用户管理组件

#### UsersComponent

**文件**: [`src/app/pages/home/users/users.ts`](src/app/pages/home/users/users.ts:1)

用户列表管理组件。

##### API 接口

```typescript
@Component({
  selector: 'app-users',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './users.html',
  styleUrls: ['./users.scss']
})
export class UsersComponent {
  users = signal<User[]>([]);
  loading = signal(false);
  searchTerm = signal('');
  currentPage = signal(1);
  totalPages = signal(1);
  
  loadUsers(): void;
  searchUsers(term: string): void;
  deleteUser(user: User): void;
  editUser(user: User): void;
}
```

##### 使用示例

```html
<!-- 用户列表 -->
<div class="users-container">
  <div class="users-header">
    <h2>用户管理</h2>
    <div class="search-box">
      <input 
        type="text" 
        [(ngModel)]="searchTerm"
        (ngModelChange)="searchUsers($event)"
        placeholder="搜索用户...">
    </div>
  </div>
  
  <div class="users-table">
    <table>
      <thead>
        <tr>
          <th>姓名</th>
          <th>邮箱</th>
          <th>角色</th>
          <th>状态</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr *ngFor="let user of users()">
          <td>{{ user.name }}</td>
          <td>{{ user.email }}</td>
          <td>{{ user.role }}</td>
          <td>
            <span [class]="'status status-' + user.status">
              {{ user.status }}
            </span>
          </td>
          <td>
            <button (click)="editUser(user)" class="btn btn-sm btn-primary">
              编辑
            </button>
            <button (click)="deleteUser(user)" class="btn btn-sm btn-danger">
              删除
            </button>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</div>
```

#### UserFormComponent

用户表单组件，用于创建和编辑用户。

##### API 接口

```typescript
@Component({
  selector: 'app-user-form',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule],
  template: `
    <form [formGroup]="userForm" (ngSubmit)="onSubmit()">
      <div class="form-group">
        <label for="name">姓名</label>
        <input 
          type="text" 
          id="name" 
          formControlName="name"
          placeholder="请输入姓名">
        <div *ngIf="userForm.get('name')?.errors" class="error">
          姓名不能为空
        </div>
      </div>
      
      <div class="form-group">
        <label for="email">邮箱</label>
        <input 
          type="email" 
          id="email" 
          formControlName="email"
          placeholder="请输入邮箱">
        <div *ngIf="userForm.get('email')?.errors" class="error">
          邮箱格式不正确
        </div>
      </div>
      
      <div class="form-group">
        <label for="role">角色</label>
        <select id="role" formControlName="role">
          <option value="admin">管理员</option>
          <option value="user">用户</option>
          <option value="guest">访客</option>
        </select>
      </div>
      
      <div class="form-actions">
        <button type="button" (click)="onCancel()" class="btn btn-secondary">
          取消
        </button>
        <button 
          type="submit" 
          [disabled]="userForm.invalid"
          class="btn btn-primary">
          保存
        </button>
      </div>
    </form>
  `
})
export class UserFormComponent {
  userForm: FormGroup;
  isEdit = signal(false);
  
  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router
  ) {
    this.buildForm();
    this.checkEditMode();
  }
  
  private buildForm(): void {
    this.userForm = this.fb.group({
      name: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      role: ['user', Validators.required]
    });
  }
  
  onSubmit(): void;
  onCancel(): void;
}
```

### 错误页面组件

#### Error404Component

**文件**: [`src/app/pages/error/404.ts`](src/app/pages/error/404.ts:1)

404 错误页面组件。

```typescript
@Component({
  selector: 'app-error-404',
  standalone: true,
  template: `
    <div class="error-page">
      <div class="error-content">
        <h1>404</h1>
        <h2>页面未找到</h2>
        <p>抱歉，您访问的页面不存在</p>
        <button (click)="goHome()" class="btn btn-primary">
          返回首页
        </button>
      </div>
    </div>
  `,
  styles: [`
    .error-page {
      display: flex;
      align-items: center;
      justify-content: center;
      min-height: 100vh;
      text-align: center;
    }
    
    .error-content h1 {
      font-size: 6rem;
      color: #dc3545;
      margin-bottom: 1rem;
    }
  `]
})
export class Error404Component {
  private router = inject(Router);
  
  goHome(): void {
    this.router.navigate(['/']);
  }
}
```

## 布局组件

### BaseLayoutComponent

**文件**: [`src/app/layout/base-layout.ts`](src/app/layout/base-layout.ts:1)

基础布局组件。

```typescript
@Component({
  selector: 'app-base-layout',
  standalone: true,
  imports: [RouterOutlet, MatProgressBarModule],
  template: `
    @if (progress()) {
      <div class="fixed-top">
        <mat-progress-bar mode="query"></mat-progress-bar>
      </div>
    }
    <router-outlet />
  `,
  styles: [`
    :host {
      min-height: 100%;
      min-width: 100%;
    }
    
    .fixed-top {
      position: fixed;
      top: 0;
      left: 0;
      right: 0;
      z-index: 9999;
    }
  `]
})
export class BaseLayoutComponent implements OnInit {
  private readonly progressBar = inject(ProgressBar);
  progress = signal(false);
  
  ngOnInit(): void {
    this.progressBar.isShow$.subscribe(isShow => {
      this.progress.set(isShow);
    });
  }
}
```

## 插件组件

### AlertsComponent

**文件**: [`src/app/plugins/alerts/alerts.ts`](src/app/plugins/alerts/alerts.ts:1)

警告提示组件。

```typescript
@Component({
  selector: 'app-alerts',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="alerts-container">
      <div 
        *ngFor="let alert of alerts()" 
        class="alert alert-{{ alert.type }}"
        [class.closable]="alert.closable"
        (click)="alert.closable && closeAlert(alert)">
        
        <div class="alert-icon">
          <i [class]="'icon-' + alert.type"></i>
        </div>
        
        <div class="alert-content">
          <div class="alert-message">{{ alert.message }}</div>
        </div>
        
        <button 
          *ngIf="alert.closable" 
          class="alert-close"
          (click)="closeAlert(alert); $event.stopPropagation()">
          ×
        </button>
      </div>
    </div>
  `,
  styles: [`
    .alerts-container {
      position: fixed;
      top: 20px;
      right: 20px;
      z-index: 9999;
      max-width: 400px;
    }
    
    .alert {
      display: flex;
      align-items: center;
      padding: 16px;
      margin-bottom: 10px;
      border-radius: 4px;
      box-shadow: 0 2px 8px rgba(0,0,0,0.15);
      animation: slideIn 0.3s ease-out;
    }
    
    .alert-success {
      background-color: #d4edda;
      border-left: 4px solid #28a745;
      color: #155724;
    }
    
    .alert-error {
      background-color: #f8d7da;
      border-left: 4px solid #dc3545;
      color: #721c24;
    }
    
    .alert-warning {
      background-color: #fff3cd;
      border-left: 4px solid #ffc107;
      color: #856404;
    }
    
    .alert-info {
      background-color: #d1ecf1;
      border-left: 4px solid #17a2b8;
      color: #0c5460;
    }
    
    @keyframes slideIn {
      from {
        transform: translateX(100%);
        opacity: 0;
      }
      to {
        transform: translateX(0);
        opacity: 1;
      }
    }
  `]
})
export class AlertsComponent {
  alerts = signal<Alert[]>([]);
  
  show(message: string, type: AlertType, options?: AlertOptions): void;
  closeAlert(alert: Alert): void;
}

interface Alert {
  id: string;
  message: string;
  type: AlertType;
  duration: number;
  closable: boolean;
  autoClose: boolean;
}
```

### ToastsComponent

**文件**: [`src/app/plugins/toasts/toasts.ts`](src/app/plugins/toasts/toasts.ts:1)

消息提示组件。

```typescript
@Component({
  selector: 'app-toasts',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="toasts-container" [class]="'position-' + position">
      <div 
        *ngFor="let toast of toasts()" 
        class="toast toast-{{ toast.type }}"
        [@toastAnimation]>
        
        <div class="toast-header" *ngIf="toast.title">
          <strong class="toast-title">{{ toast.title }}</strong>
          <button 
            *ngIf="toast.closable" 
            class="toast-close"
            (click)="closeToast(toast)">
            ×
          </button>
        </div>
        
        <div class="toast-body">
          <div class="toast-message">{{ toast.message }}</div>
          <div class="toast-progress" *ngIf="toast.showProgress">
            <div 
              class="toast-progress-bar" 
              [style.width.%]="toast.progress">
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
  animations: [
    trigger('toastAnimation', [
      transition(':enter', [
        style({ opacity: 0, transform: 'translateY(-20px)' }),
        animate('300ms ease-out', style({ opacity: 1, transform: 'translateY(0)' }))
      ]),
      transition(':leave', [
        animate('300ms ease-in', style({ opacity: 0, transform: 'translateX(100%)' }))
      ])
    ])
  ],
  styles: [`
    .toasts-container {
      position: fixed;
      z-index: 9999;
      pointer-events: none;
    }
    
    .position-top-right {
      top: 20px;
      right: 20px;
    }
    
    .position-top-left {
      top: 20px;
      left: 20px;
    }
    
    .position-bottom-right {
      bottom: 20px;
      right: 20px;
    }
    
    .position-bottom-left {
      bottom: 20px;
      left: 20px;
    }
    
    .toast {
      min-width: 300px;
      max-width: 500px;
      margin-bottom: 10px;
      background: white;
      border-radius: 4px;
      box-shadow: 0 4px 12px rgba(0,0,0,0.15);
      pointer-events: auto;
    }
    
    .toast-success {
      border-left: 4px solid #28a745;
    }
    
    .toast-error {
      border-left: 4px solid #dc3545;
    }
    
    .toast-warning {
      border-left: 4px solid #ffc107;
    }
    
    .toast-info {
      border-left: 4px solid #17a2b8;
    }
  `]
})
export class ToastsComponent {
  toasts = signal<Toast[]>([]);
  position = signal<ToastPosition>('top-right');
  
  show(message: string, title?: string, type?: ToastType, options?: ToastOptions): void;
  closeToast(toast: Toast): void;
}
```

### ModalsComponent

**文件**: [`src/app/plugins/modals/modals.ts`](src/app/plugins/modals/modals.ts:1)

模态框组件。

```typescript
@Component({
  selector: 'app-modals',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="modal-backdrop" *ngIf="hasModals()" (click)="onBackdropClick()">
      <div 
        *ngFor="let modal of modals()" 
        class="modal"
        [class]="'modal-' + modal.size"
        (click)="$event.stopPropagation()">
        
        <div class="modal-content">
          <div class="modal-header" *ngIf="modal.showHeader">
            <h5 class="modal-title">{{ modal.title }}</h5>
            <button 
              *ngIf="modal.closeButton" 
              class="modal-close"
              (click)="closeModal(modal)">
              ×
            </button>
          </div>
          
          <div class="modal-body">
            <ng-container *ngComponentOutlet="modal.component; inputs: modal.data"></ng-container>
          </div>
          
          <div class="modal-footer" *ngIf="modal.showFooter">
            <button 
              *ngIf="modal.showCancelButton" 
              class="btn btn-secondary"
              (click)="onCancel(modal)">
              {{ modal.cancelText }}
            </button>
            <button 
              *ngIf="modal.showOkButton" 
              class="btn btn-primary"
              (click)="onOk(modal)">
              {{ modal.okText }}
            </button>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .modal-backdrop {
      position: fixed;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      background-color: rgba(0, 0, 0, 0.5);
      display: flex;
      align-items: center;
      justify-content: center;
      z-index: 9999;
    }
    
    .modal {
      background: white;
      border-radius: 8px;
      box-shadow: 0 8px 24px rgba(0,0,0,0.15);
      max-height: 90vh;
      overflow: hidden;
    }
    
    .modal-sm {
      width: 300px;
    }
    
    .modal-md {
      width: 500px;
    }
    
    .modal-lg {
      width: 800px;
    }
    
    .modal-xl {
      width: 1140px;
    }
    
    .modal-fullscreen {
      width: 100%;
      height: 100%;
      max-height: 100vh;
      border-radius: 0;
    }
  `]
})
export class ModalsComponent {
  modals = signal<Modal[]>([]);
  
  open<T>(component: Type<T>, options?: ModalOptions): ModalRef<T>;
  closeModal(modal: Modal): void;
  closeAll(): void;
}
```

## 组件开发最佳实践

### 1. 组件设计原则

```typescript
// ✅ 好的实践：单一职责
@Component({
  selector: 'app-user-card',
  standalone: true,
  template: `
    <div class="user-card">
      <img [src]="user().avatar" [alt]="user().name">
      <h3>{{ user().name }}</h3>
      <p>{{ user().email }}</p>
    </div>
  `
})
export class UserCardComponent {
  @Input() user = input.required<User>();
  @Output() selected = output<User>();
}

// ❌ 避免：组件职责过多
@Component({
  selector: 'app-user-manager',
  template: `
    <!-- 包含数据获取、表单处理、列表展示等多个职责 -->
  `
})
export class UserManagerComponent {
  // 过多的职责
}
```

### 2. 输入输出设计

```typescript
// ✅ 使用信号输入（Angular v20+）
@Component({
  selector: 'app-counter',
  standalone: true,
  template: `
    <div class="counter">
      <button (click)="decrement()">-</button>
      <span>{{ count() }}</span>
      <button (click)="increment()">+</button>
    </div>
  `
})
export class CounterComponent {
  count = input(0);  // 信号输入
  countChange = output<number>();  // 输出事件
  
  increment(): void {
    this.countChange.emit(this.count() + 1);
  }
  
  decrement(): void {
    this.countChange.emit(this.count() - 1);
  }
}

// 使用示例
<app-counter 
  [count]="currentCount" 
  (countChange)="onCountChange($event)">
</app-counter>
```

### 3. 变更检测优化

```typescript
// ✅ 使用 OnPush 策略
@Component({
  selector: 'app-optimized',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div>{{ data() }}</div>
    <button (click)="updateData()">更新</button>
  `
})
export class OptimizedComponent {
  data = signal<string>('初始数据');
  
  updateData(): void {
    this.data.set('新数据'); // 自动触发变更检测
  }
}

// ✅ 使用信号
@Component({
  selector: 'app-signals',
  template: `
    <div>{{ user().name }}</div>
    <button (click)="updateUser()">更新用户</button>
  `
})
export class SignalsComponent {
  user = signal<User>({ name: 'John', age: 30 });
  
  updateUser(): void {
    this.user.update(user => ({ ...user, name: 'Jane' }));
  }
}
```

### 4. 组件通信

```typescript
// 父子组件通信
@Component({
  selector: 'app-parent',
  template: `
    <app-child 
      [data]="parentData" 
      (dataChange)="onChildChange($event)">
    </app-child>
  `
})
export class ParentComponent {
  parentData = { message: 'Hello' };
  
  onChildChange(data: any): void {
    console.log('子组件数据变化:', data);
  }
}

// 兄弟组件通信（通过服务）
@Injectable({
  providedIn: 'root'
})
export class ComponentCommunicationService {
  private messageSubject = new Subject<string>();
  message$ = this.messageSubject.asObservable();
  
  sendMessage(message: string): void {
    this.messageSubject.next(message);
  }
}

// 使用示例
@Component({
  selector: 'app-sender',
  template: `<button (click)="send()">发送消息</button>`
})
export class SenderComponent {
  constructor(private commService: ComponentCommunicationService) {}
  
  send(): void {
    this.commService.sendMessage('Hello from sender');
  }
}

@Component({
  selector: 'app-receiver',
  template: `<div>{{ message }}</div>`
})
export class ReceiverComponent implements OnInit {
  message = '';
  
  constructor(private commService: ComponentCommunicationService) {}
  
  ngOnInit(): void {
    this.commService.message$.subscribe(msg => {
      this.message = msg;
    });
  }
}
```

### 5. 组件测试

```typescript
// 组件测试示例
describe('UserCardComponent', () => {
  let component: UserCardComponent;
  let fixture: ComponentFixture<UserCardComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UserCardComponent]
    }).compileComponents();

    fixture = TestBed.createComponent(UserCardComponent);
    component = fixture.componentInstance;
  });

  it('should display user information', () => {
    const mockUser: User = {
      id: 1,
      name: 'John Doe',
      email: 'john@example.com',
      avatar: 'avatar.jpg'
    };
    
    component.user.set(mockUser);
    fixture.detectChanges();
    
    const nameElement = fixture.nativeElement.querySelector('h3');
    const emailElement = fixture.nativeElement.querySelector('p');
    
    expect(nameElement.textContent).toContain('John Doe');
    expect(emailElement.textContent).toContain('john@example.com');
  });

  it('should emit selected event', () => {
    const mockUser: User = { id: 1, name: 'John', email: 'john@example.com', avatar: 'avatar.jpg' };
    component.user.set(mockUser);
    
    let selectedUser: User | undefined;
    component.selected.subscribe(user => {
      selectedUser = user;
    });
    
    fixture.nativeElement.querySelector('.user-card').click();
    
    expect(selectedUser).toEqual(mockUser);
  });
});
```

## 组件样式指南

### 1. 样式组织

```scss
// 组件样式文件：user-card.component.scss
.user-card {
  display: flex;
  align-items: center;
  padding: 16px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  transition: transform 0.2s ease;
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
  }
  
  .user-avatar {
    width: 48px;
    height: 48px;
    border-radius: 50%;
    margin-right: 16px;
  }
  
  .user-info {
    flex: 1;
    
    .user-name {
      font-size: 16px;
      font-weight: 600;
      margin-bottom: 4px;
    }
    
    .user-email {
      font-size: 14px;
      color: #666;
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .user-card {
    padding: 12px;
    
    .user-avatar {
      width: 40px;
      height: 40px;
    }
  }
}
```

### 2. CSS 变量使用

```scss
// 定义 CSS 变量
:root {
  --primary-color: #1976d2;
  --secondary-color: #424242;
  --success-color: #4caf50;
  --error-color: #f44336;
  --warning-color: #ff9800;
  --info-color: #2196f3;
  
  --border-radius: 4px;
  --box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  --transition: all 0.3s ease;
}

// 在组件中使用
.alert {
  background-color: var(--info-color);
  border-radius: var(--border-radius);
  box-shadow: var(--box-shadow);
  transition: var(--transition);
}
```

### 3. 主题支持

```typescript
// 主题服务
@Injectable({
  providedIn: 'root'
})
export class ThemeService {
  private currentTheme = signal<'light' | 'dark'>('light');
  
  setTheme(theme: 'light' | 'dark'): void {
    this.currentTheme.set(theme);
    document.documentElement.setAttribute('data-theme', theme);
  }
  
  getCurrentTheme() {
    return this.currentTheme.asReadonly();
  }
}

// 在组件中使用
@Component({
  selector: 'app-themed',
  template: `
    <div class="themed-component" [attr.data-theme]="theme()">
      <!-- 组件内容 -->
    </div>
  `,
  styles: [`
    .themed-component {
      &[data-theme="light"] {
        background: white;
        color: black;
      }
      
      &[data-theme="dark"] {
        background: #1a1a1a;
        color: white;
      }
    }
  `]
})
export class ThemedComponent {
  theme = inject(ThemeService).getCurrentTheme();
}
```

---

**文档版本**: 1.0.0  
**最后更新**: 2025年

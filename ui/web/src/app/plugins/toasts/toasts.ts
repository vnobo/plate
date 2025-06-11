import { CommonModule } from '@angular/common';
import {
  ApplicationRef,
  Component,
  EnvironmentInjector,
  Injectable,
  OnDestroy,
  afterEveryRender,
  createComponent,
  inject,
  signal,
} from '@angular/core';

// Toast类型定义
export type ToastType = 'success' | 'error' | 'warning' | 'info';

// Toast消息接口
export interface Toast {
  id: string;
  title: string;
  message: string;
  type: ToastType;
  avatar?: string;
  timestamp: Date;
  autohide?: boolean;
  delay?: number;
}

// Toast服务
@Injectable({
  providedIn: 'root',
})
export class ToastService {
  constructor(private appRef: ApplicationRef, private injector: EnvironmentInjector) {}

  open(text: string) {
    // Create a `ComponentRef` instance.
    const dialogRef = createComponent(Toasts, {
      environmentInjector: this.injector,
    });

    dialogRef.setInput('text', text);

    document.body.appendChild(dialogRef.location.nativeElement);

    // Register the newly created ref using the `ApplicationRef` instance
    // to include the component view into change detection cycles.
    this.appRef.attachView(dialogRef.hostView);
  }
  /**
   * 添加一个新的toast消息
   * @param title 标题
   * @param message 消息内容
   * @param type 消息类型
   * @param options 额外选项
   */
  show(
    title: string,
    message: string,
    type: ToastType = 'info',
    options: Partial<Omit<Toast, 'id' | 'title' | 'message' | 'type' | 'timestamp'>> = {},
  ): string {
    const id = this.generateId();
    const toast: Toast = {
      id,
      title,
      message,
      type,
      timestamp: new Date(),
      autohide: options.autohide ?? true,
      delay: options.delay ?? 5000,
      avatar: options.avatar,
    };

    // 添加到列表
    this.toasts.update(list => [...list, toast]);

    // 如果设置了自动隐藏，则设置定时器
    if (toast.autohide) {
      setTimeout(() => {
        this.remove(id);
      }, toast.delay);
    }

    return id;
  }

  /**
   * 显示成功消息
   */
  success(
    title: string,
    message: string,
    options: Partial<Omit<Toast, 'id' | 'title' | 'message' | 'type' | 'timestamp'>> = {},
  ): string {
    return this.show(title, message, 'success', options);
  }

  /**
   * 显示错误消息
   */
  error(
    title: string,
    message: string,
    options: Partial<Omit<Toast, 'id' | 'title' | 'message' | 'type' | 'timestamp'>> = {},
  ): string {
    return this.show(title, message, 'error', options);
  }

  /**
   * 显示警告消息
   */
  warning(
    title: string,
    message: string,
    options: Partial<Omit<Toast, 'id' | 'title' | 'message' | 'type' | 'timestamp'>> = {},
  ): string {
    return this.show(title, message, 'warning', options);
  }

  /**
   * 显示信息消息
   */
  info(
    title: string,
    message: string,
    options: Partial<Omit<Toast, 'id' | 'title' | 'message' | 'type' | 'timestamp'>> = {},
  ): string {
    return this.show(title, message, 'info', options);
  }

  /**
   * 移除指定ID的toast
   */
  remove(id: string): void {
    this.toasts.update(list => list.filter(toast => toast.id !== id));
  }

  /**
   * 清除所有toast
   */
  clear(): void {
    this.toasts.set([]);
  }

  /**
   * 生成唯一ID
   */
  private generateId(): string {
    return `toast-${Date.now()}-${Math.floor(Math.random() * 1000)}`;
  }
}

@Component({
  selector: 'tabler-toasts',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="toast-container p-3 bottom-0 end-0 ">
      @for (toast of toasts(); track toast.id) {
      <div
        role="alert"
        aria-live="assertive"
        aria-atomic="true"
        class="toast align-items-center text-bg-success border-0"
        data-bs-toggle="toast"
        [attr.data-bs-delay]="toast.delay || 3000"
        [attr.data-bs-autohide]="toast.autohide">
        <div class="d-flex">
          <div class="toast-body">{{ toast.message }}</div>
          <button
            type="button"
            class="btn-close btn-close-white me-2 m-auto"
            data-bs-dismiss="toast"
            aria-label="Close"></button>
        </div>
      </div>
      }
    </div>
  `,
  styles: `
    .toast-container {
      z-index: 1050;
    }
  `,
})
export class Toasts implements OnDestroy {
  // 使用signal管理toast列表
  toasts = signal<Toast[]>([]);

  // 构造函数
  constructor() {
    afterEveryRender(() => {});
  }

  remove(id: string) {
    this.toasts.update(list => list.filter(toast => toast.id !== id));
  }

  clear() {
    this.toasts.set([]);
  }

  ngOnDestroy() {
    this.clear();
  }
}

import { Component, Injectable, OnDestroy, computed, effect, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';

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
  // 使用signal管理toast列表
  private toasts = signal<Toast[]>([]);

  // 公开的只读toast列表
  public readonly toastList = this.toasts.asReadonly();

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
    <div class="toast-container position-fixed top-0 end-0 p-3">
      @for (toast of toastService.toastList(); track toast.id) {
      <div
        class="toast show"
        [ngClass]="getToastClass(toast.type)"
        role="alert"
        aria-live="assertive"
        aria-atomic="true">
        <div class="toast-header">
          @if (toast.avatar) {
          <span
            class="avatar avatar-xs me-2"
            [style.background-image]="'url(' + toast.avatar + ')'"></span>
          } @else {
          <span class="toast-icon me-2" [ngClass]="getIconClass(toast.type)"></span>
          }
          <strong class="me-auto">{{ toast.title }}</strong>
          <small>{{ getTimeAgo(toast.timestamp) }}</small>
          <button
            type="button"
            class="ms-2 btn-close"
            (click)="closeToast(toast.id)"
            aria-label="Close"></button>
        </div>
        <div class="toast-body">{{ toast.message }}</div>
      </div>
      }
    </div>
  `,
  styles: `
    .toast-container {
      z-index: 1050;
    }
    .toast-icon {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      width: 24px;
      height: 24px;
      border-radius: 50%;
    }
    .toast-icon.success {
      background-color: #5eba00;
      color: white;
    }
    .toast-icon.error {
      background-color: #cd201f;
      color: white;
    }
    .toast-icon.warning {
      background-color: #f59f00;
      color: white;
    }
    .toast-icon.info {
      background-color: #467fcf;
      color: white;
    }
    .toast.success {
      border-left: 4px solid #5eba00;
    }
    .toast.error {
      border-left: 4px solid #cd201f;
    }
    .toast.warning {
      border-left: 4px solid #f59f00;
    }
    .toast.info {
      border-left: 4px solid #467fcf;
    }
  `,
})
export class Toasts implements OnDestroy {
  // 注入Toast服务
  toastService = inject(ToastService);

  // 构造函数
  constructor() {
    // 可以在这里添加初始化逻辑
  }

  // 获取Toast类型对应的CSS类
  getToastClass(type: ToastType): string {
    return type;
  }

  // 获取Toast类型对应的图标类
  getIconClass(type: ToastType): string {
    return type;
  }

  // 计算时间差显示
  getTimeAgo(timestamp: Date): string {
    const now = new Date();
    const diff = Math.floor((now.getTime() - timestamp.getTime()) / 1000);

    if (diff < 60) {
      return '刚刚';
    } else if (diff < 3600) {
      return `${Math.floor(diff / 60)} 分钟前`;
    } else if (diff < 86400) {
      return `${Math.floor(diff / 3600)} 小时前`;
    } else {
      return `${Math.floor(diff / 86400)} 天前`;
    }
  }

  // 关闭指定的Toast
  closeToast(id: string): void {
    this.toastService.remove(id);
  }

  // 组件销毁时的清理工作
  ngOnDestroy(): void {
    // 可以在这里添加清理逻辑
  }
}

import {
  Component,
  Injectable,
  OnDestroy,
  OnInit,
  Signal,
  WritableSignal,
  computed,
  inject,
  signal,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { Subject } from 'rxjs';

/**
 * 警告框类型
 */
export type AlertType = 'success' | 'info' | 'warning' | 'danger';

/**
 * 警告框配置选项
 */
export interface AlertOptions {
  /** 是否自动隐藏 */
  autohide?: boolean;
  /** 自动隐藏延迟时间（毫秒） */
  delay?: number;
  /** 是否可关闭 */
  dismissible?: boolean;
  /** 是否显示图标 */
  showIcon?: boolean;
}

/**
 * 警告框数据
 */
export interface AlertData {
  /** 唯一标识符 */
  id: string;
  /** 警告框类型 */
  type: AlertType;
  /** 标题 */
  title: string;
  /** 内容 */
  message: string;
  /** 配置选项 */
  options: AlertOptions;
  /** 创建时间 */
  createdAt: number;
}

/**
 * 默认警告框配置
 */
const DEFAULT_ALERT_OPTIONS: AlertOptions = {
  autohide: false,
  delay: 5000,
  dismissible: true,
  showIcon: true,
};

/**
 * 警告框服务
 * 用于创建和管理警告框
 */
@Injectable({
  providedIn: 'root',
})
export class AlertService {
  private _alerts: WritableSignal<AlertData[]> = signal([]);
  private _autoHideTimers: Map<string, any> = new Map();

  /**
   * 获取当前所有警告框
   */
  public get alerts(): Signal<AlertData[]> {
    return computed(() => this._alerts());
  }

  /**
   * 创建成功类型的警告框
   * @param title 标题
   * @param message 内容
   * @param options 配置选项
   * @returns 警告框ID
   */
  public success(title: string, message: string, options?: AlertOptions): string {
    return this.show('success', title, message, options);
  }

  /**
   * 创建信息类型的警告框
   * @param title 标题
   * @param message 内容
   * @param options 配置选项
   * @returns 警告框ID
   */
  public info(title: string, message: string, options?: AlertOptions): string {
    return this.show('info', title, message, options);
  }

  /**
   * 创建警告类型的警告框
   * @param title 标题
   * @param message 内容
   * @param options 配置选项
   * @returns 警告框ID
   */
  public warning(title: string, message: string, options?: AlertOptions): string {
    return this.show('warning', title, message, options);
  }

  /**
   * 创建危险类型的警告框
   * @param title 标题
   * @param message 内容
   * @param options 配置选项
   * @returns 警告框ID
   */
  public danger(title: string, message: string, options?: AlertOptions): string {
    return this.show('danger', title, message, options);
  }

  /**
   * 显示警告框
   * @param type 类型
   * @param title 标题
   * @param message 内容
   * @param options 配置选项
   * @returns 警告框ID
   */
  public show(type: AlertType, title: string, message: string, options?: AlertOptions): string {
    const id = this.generateId();
    const alertOptions = { ...DEFAULT_ALERT_OPTIONS, ...options };

    const alert: AlertData = {
      id,
      type,
      title,
      message,
      options: alertOptions,
      createdAt: Date.now(),
    };

    this._alerts.update(alerts => [...alerts, alert]);

    if (alertOptions.autohide) {
      this.setAutoHideTimer(id, alertOptions.delay!);
    }

    return id;
  }

  /**
   * 隐藏指定ID的警告框
   * @param id 警告框ID
   */
  public hide(id: string): void {
    this.clearAutoHideTimer(id);
    this._alerts.update(alerts => alerts.filter(alert => alert.id !== id));
  }

  /**
   * 隐藏所有警告框
   */
  public hideAll(): void {
    this._alerts().forEach(alert => this.clearAutoHideTimer(alert.id));
    this._alerts.set([]);
  }

  /**
   * 设置自动隐藏定时器
   * @param id 警告框ID
   * @param delay 延迟时间
   */
  private setAutoHideTimer(id: string, delay: number): void {
    this.clearAutoHideTimer(id);
    const timer = setTimeout(() => {
      this.hide(id);
    }, delay);
    this._autoHideTimers.set(id, timer);
  }

  /**
   * 清除自动隐藏定时器
   * @param id 警告框ID
   */
  private clearAutoHideTimer(id: string): void {
    if (this._autoHideTimers.has(id)) {
      clearTimeout(this._autoHideTimers.get(id));
      this._autoHideTimers.delete(id);
    }
  }

  /**
   * 生成唯一ID
   * @returns 唯一ID
   */
  private generateId(): string {
    return `alert-${Date.now()}-${Math.floor(Math.random() * 1000)}`;
  }
}

/**
 * 警告框组件
 * 用于显示警告框
 */
@Component({
  selector: 'tabler-alerts',
  imports: [CommonModule],
  template: `
    @for (alert of alertService.alerts(); track alert.id) {
    <div
      class="alert alert-{{ alert.type }} mb-3"
      role="alert"
      [class.alert-dismissible]="alert.options.dismissible">
      @if (alert.options.showIcon) {
      <div class="alert-icon">
        @switch (alert.type) { @case ('success') {
        <svg
          xmlns="http://www.w3.org/2000/svg"
          width="24"
          height="24"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          stroke-width="2"
          stroke-linecap="round"
          stroke-linejoin="round"
          class="icon alert-icon icon-2">
          <path d="M5 12l5 5l10 -10" />
        </svg>
        } @case ('info') {
        <svg
          xmlns="http://www.w3.org/2000/svg"
          width="24"
          height="24"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          stroke-width="2"
          stroke-linecap="round"
          stroke-linejoin="round"
          class="icon alert-icon icon-2">
          <path d="M3 12a9 9 0 1 0 18 0a9 9 0 0 0 -18 0" />
          <path d="M12 9h.01" />
          <path d="M11 12h1v4h1" />
        </svg>
        } @case ('warning') {
        <svg
          xmlns="http://www.w3.org/2000/svg"
          width="24"
          height="24"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          stroke-width="2"
          stroke-linecap="round"
          stroke-linejoin="round"
          class="icon alert-icon icon-2">
          <path d="M12 9v4" />
          <path
            d="M10.363 3.591l-8.106 13.534a1.914 1.914 0 0 0 1.636 2.871h16.214a1.914 1.914 0 0 0 1.636 -2.87l-8.106 -13.536a1.914 1.914 0 0 0 -3.274 0z" />
          <path d="M12 16h.01" />
        </svg>
        } @case ('danger') {
        <svg
          xmlns="http://www.w3.org/2000/svg"
          width="24"
          height="24"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          stroke-width="2"
          stroke-linecap="round"
          stroke-linejoin="round"
          class="icon alert-icon icon-2">
          <path d="M3 12a9 9 0 1 0 18 0a9 9 0 0 0 -18 0" />
          <path d="M12 8v4" />
          <path d="M12 16h.01" />
        </svg>
        } }
      </div>
      }
      <div>
        <h4 class="alert-heading">{{ alert.title }}</h4>
        <div class="alert-description">{{ alert.message }}</div>
      </div>
      @if (alert.options.dismissible) {
      <a class="btn-close" (click)="closeAlert(alert.id)"></a>
      }
    </div>
    }
  `,
  styles: `
  `,
})
export class Alerts implements OnInit, OnDestroy {
  private readonly destroy$ = new Subject<void>();
  public readonly alertService = inject(AlertService);

  ngOnInit(): void {
    // 组件初始化逻辑
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  /**
   * 关闭警告框
   * @param id 警告框ID
   */
  closeAlert(id: string): void {
    this.alertService.hide(id);
  }
}

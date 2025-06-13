import { CommonModule } from '@angular/common';
import {
  afterNextRender,
  ApplicationRef,
  Component,
  ComponentRef,
  createComponent,
  EnvironmentInjector,
  Injectable,
  model,
  OnDestroy,
  OnInit,
} from '@angular/core';
import { animate, style, transition, trigger } from '@angular/animations';

// Toast类型定义
export type ToastType = 'success' | 'error' | 'warning' | 'info';

// Toast消息接口
export interface ToastMessage {
  id: string;
  title?: string;
  message: string;
  type: ToastType;
  autohide?: boolean;
  delay?: number;
}

// Toast服务
@Injectable({
  providedIn: 'root',
})
export class MessageService {
  private toastRef: ComponentRef<Toasts> | undefined | null = null;

  constructor(private appRef: ApplicationRef, private injector: EnvironmentInjector) {}

  create() {
    this.toastRef = createComponent(Toasts, {
      environmentInjector: this.injector,
    });

    document.body.appendChild(this.toastRef.location.nativeElement);
    this.appRef.attachView(this.toastRef.hostView);
  }

  show(
    title: string,
    message: string,
    type: ToastType = 'info',
    options: Partial<Omit<ToastMessage, 'id' | 'title' | 'message' | 'type'>> = {},
  ) {
    const id = this.generateId();
    const toast: ToastMessage = {
      id,
      title,
      message,
      type,
      autohide: options.autohide ?? true,
      delay: options.delay ?? 5000,
    };
    if (!this.toastRef) {
      this.create();
    }
    if (this.toastRef) {
      this.toastRef.instance.add(toast);
    }
    return id;
  }

  success(
    title: string,
    message: string,
    options: Partial<Omit<ToastMessage, 'id' | 'title' | 'message' | 'type'>> = {},
  ): string {
    return this.show(title, message, 'success', options);
  }

  error(
    title: string,
    message: string,
    options: Partial<Omit<ToastMessage, 'id' | 'title' | 'message' | 'type'>> = {},
  ): string {
    return this.show(title, message, 'error', options);
  }

  warning(
    title: string,
    message: string,
    options: Partial<Omit<ToastMessage, 'id' | 'title' | 'message' | 'type'>> = {},
  ): string {
    return this.show(title, message, 'warning', options);
  }

  info(
    title: string,
    message: string,
    options: Partial<Omit<ToastMessage, 'id' | 'title' | 'message' | 'type'>> = {},
  ): string {
    return this.show(title, message, 'info', options);
  }

  private generateId(): string {
    return `toast-${Date.now()}-${Math.floor(Math.random() * 1000)}`;
  }
}

@Component({
  selector: 'tabler-toasts',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="toast-container p-3 bottom-0 end-0">
      @for (toast of toasts(); track toast.id) {
      <div
        [attr.data-toast-id]="toast.id"
        role="alert"
        aria-live="assertive"
        aria-atomic="true"
        class="toast align-items-center text-bg-{{ toast.type }} border-0">
        <div class="d-flex">
          <div class="toast-body">{{ toast.message }}</div>
          <button
            type="button"
            class="btn-close btn-close-white me-2 m-auto"
            (click)="remove(toast.id)"
            aria-label="Close"></button>
        </div>
      </div>
      }
    </div>
  `,
  styles: `
    :host {
      position: fixed;
      bottom: 0;
      right: 0;
      z-index: 1050;
    }
  `,
  animations: [
    trigger('toastAnimation', [
      transition(':enter', [
        style({ opacity: 0, transform: 'translateX(100%)' }),
        animate('300ms ease-out', style({ opacity: 1, transform: 'translateX(0)' })),
      ]),
      transition(':leave', [
        animate('300ms ease-in', style({ opacity: 0, transform: 'translateX(100%)' })),
      ]),
    ]),
  ],
})
export class Toasts implements OnInit, OnDestroy {
  // 使用signal管理toast列表
  toasts = model<ToastMessage[]>([]);

  constructor() {
    afterNextRender(() => {
      //const toastEl = document.querySelector('.toast');
      //if (toastEl) {
      //  const toast = new Toast(toastEl, {
      //    autohide: true,
      //    delay: 3000,
      //  });
      //  toast.show();
      //}
    });
  }

  ngOnInit(): void {
    console.log('ToastComponent init');
  }

  public add(toast: ToastMessage): void {
    this.toasts.update(list => [...list, toast]);
  }

  remove(id: string): void {
    this.toasts.update(list => list.filter(t => t.id !== id));
  }

  clear(): void {
    this.toasts.set([]);
  }

  ngOnDestroy(): void {
    this.clear();
  }
}

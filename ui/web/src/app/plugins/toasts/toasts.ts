import { CommonModule } from '@angular/common';
import {
  afterNextRender,
  ApplicationRef,
  Component,
  ComponentRef,
  computed,
  createComponent,
  Directive,
  ElementRef,
  EnvironmentInjector,
  inject,
  Injectable,
  OnDestroy,
  output,
  signal,
} from '@angular/core';
import { trigger, transition, style, animate } from '@angular/animations';

// Toast类型定义
export type ToastType = 'success' | 'danger' | 'warning' | 'info';

// Toast消息接口
export interface Message {
  id: string;
  message: string;
  type: ToastType;
  animation?: boolean;
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
    this.toastRef.instance.toastsDropped.subscribe(() => {
      this.toastRef?.destroy();
      this.toastRef = null;
    });
  }

  show(
    message: string,
    type: ToastType = 'info',
    options: Partial<Omit<Message, 'id' | 'message' | 'type'>> = {},
  ) {
    const id = this.generateCryptoId();
    const toast: Message = {
      id,
      message,
      type,
      animation: options.animation ?? true,
      autohide: options.autohide ?? true,
      delay: options.delay ?? 5000,
    };
    if (!this.toastRef || this.toastRef === null || this.toastRef === undefined) {
      this.create();
    }
    if (this.toastRef) {
      this.toastRef.instance.add(toast);
    }
    return id;
  }

  success(
    message: string,
    options: Partial<Omit<Message, 'id' | 'message' | 'type'>> = {},
  ): string {
    return this.show(message, 'success', options);
  }

  error(message: string, options: Partial<Omit<Message, 'id' | 'message' | 'type'>> = {}): string {
    return this.show(message, 'danger', options);
  }

  warning(
    message: string,
    options: Partial<Omit<Message, 'id' | 'message' | 'type'>> = {},
  ): string {
    return this.show(message, 'warning', options);
  }

  info(message: string, options: Partial<Omit<Message, 'id' | 'message' | 'type'>> = {}): string {
    return this.show(message, 'info', options);
  }

  private generateCryptoId() {
    const array = new Uint8Array(16);
    crypto.getRandomValues(array);
    return Array.from(array, byte => byte.toString(16).padStart(2, '0')).join('');
  }
}

@Directive({
  selector: '[tablerToastInit]',
})
export class TablerToastInit {
  private readonly el = inject(ElementRef);
  onHidden = output<string>();
  constructor() {
    afterNextRender(async () => {
      const tabler = await import('@tabler/core');
      const ele = this.el.nativeElement;
      const toast = tabler.Toast.getOrCreateInstance(ele);
      ele.addEventListener('hidden.bs.toast', () => this.onHidden.emit(ele.id));
      toast.show();
    });
  }
}

@Component({
  selector: 'tabler-toasts',
  standalone: true,
  imports: [CommonModule, TablerToastInit],
  host: {
    class: 'overflow-hidden',
    'aria-live': 'polite',
    'aria-atomic': 'true',
  },
  template: `
    <div class="toast-container bottom-0 end-0 p-3 overflow-hidden">
      @for (toast of msgs(); track toast) {
      <div
        id="{{ toast.id }}"
        class="toast align-items-center border-0"
        role="alert"
        aria-live="assertive"
        aria-atomic="true"
        data-bs-toggle="toast"
        [@toastAnimation]
        [ngClass]="{
          'text-bg-success': toast.type === 'success',
          'text-bg-danger': toast.type === 'danger',
          'text-bg-warning': toast.type === 'warning',
          'text-bg-info': toast.type === 'info',
          'text-bg-primary': toast.type === undefined
        }"
        [attr.data-bs-animation]="toast.animation"
        [attr.data-bs-delay]="toast.delay"
        [attr.data-bs-autohide]="toast.autohide"
        tablerToastInit
        (onHidden)="remove($event)">
        <div class="d-flex">
          <div class="toast-body">
            @if(toast.type === 'success') {
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
              class="icon icon-2">
              <path d="M5 12l5 5l10 -10" />
            </svg>
            } @else if(toast.type === 'danger') {
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
              class="icon icon-2">
              <path d="M3 12a9 9 0 1 0 18 0a9 9 0 0 0 -18 0" />
              <path d="M12 8v4" />
              <path d="M12 16h.01" />
            </svg>
            } @else if(toast.type === 'warning') {
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
              class="icon icon-2">
              <path d="M12 9v4" />
              <path
                d="M10.363 3.591l-8.106 13.534a1.914 1.914 0 0 0 1.636 2.871h16.214a1.914 1.914 0 0 0 1.636 -2.87l-8.106 -13.536a1.914 1.914 0 0 0 -3.274 0z" />
              <path d="M12 16h.01" />
            </svg>
            } @else if(toast.type === 'info') {
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
              class="icon icon-2">
              <path d="M3 12a9 9 0 1 0 18 0a9 9 0 0 0 -18 0" />
              <path d="M12 9h.01" />
              <path d="M11 12h1v4h1" />
            </svg>
            }@else {
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
              class="icon icon-2">
              <path d="M3 12a9 9 0 1 0 18 0a9 9 0 0 0 -18 0" />
              <path d="M12 9h.01" />
              <path d="M11 12h1v4h1" />
            </svg>
            }
            {{ toast.message }}
          </div>
          <button
            type="button"
            class="btn-close btn-close-white me-2 m-auto"
            data-bs-dismiss="toast"
            (click)="remove(toast.id)"
            aria-label="Close"></button>
        </div>
      </div>
      }
    </div>
  `,
  styles: [
    `
      :host {
        max-width: 100%;
      }
    `,
  ],
  animations: [
    trigger('toastAnimation', [
      transition(':enter', [
        style({ transform: 'translateX(100%)', opacity: 0 }),
        animate('500ms ease-out', style({ transform: 'translateX(0)', opacity: 1 })),
      ]),
      transition(':leave', [
        animate('300ms ease-in', style({ transform: 'translateX(100%)', opacity: 0 })),
      ]),
    ]),
  ],
})
export class Toasts implements OnDestroy {
  msgs = signal<Message[]>([]);
  toastCount = computed(() => this.msgs().length);
  toastsDropped = output<void>();

  add(toast: Message): void {
    this.msgs.update(list => [...list, toast]);
  }

  remove(id: string): void {
    this.msgs.update(list => list.filter(t => t.id !== id));
    if (this.toastCount() === 0) {
      this.toastsDropped.emit();
    }
  }

  ngOnDestroy(): void {
    this.msgs.set([]);
  }
}

import { CommonModule } from '@angular/common';
import {
  afterNextRender,
  ApplicationRef,
  Component,
  ComponentRef,
  computed,
  createComponent,
  Directive,
  DOCUMENT,
  ElementRef,
  EnvironmentInjector,
  inject,
  Injectable,
  OnDestroy,
  output,
  signal,
} from '@angular/core';
import { fromEvent, Subscription } from 'rxjs';

export type ToastType = 'success' | 'danger' | 'warning' | 'info';

export interface Message {
  id: string;
  message: string;
  type: ToastType;
  animation?: boolean;
  autohide?: boolean;
  delay?: number;
}

@Injectable({
  providedIn: 'root',
})
export class MessageService {
  private readonly document = inject(DOCUMENT);
  private toastRef: ComponentRef<Toasts> | null = null;
  private readonly DEFAULT_TOAST_OPTIONS: Required<
    Pick<Message, 'animation' | 'autohide' | 'delay'>
  > = {
    animation: true,
    autohide: true,
    delay: 100000,
  };

  constructor(private appRef: ApplicationRef) {}

  /**
   * Creates a toast component and attaches it to the DOM
   */
  private create() {
    if (this.toastRef) {
      return; // Already exists
    }

    this.toastRef = createComponent(Toasts, {
      environmentInjector: this.appRef.injector,
    });

    // Append to body and attach to application ref
    this.document.body.appendChild(this.toastRef.location.nativeElement);
    this.appRef.attachView(this.toastRef.hostView);
    this.toastRef.changeDetectorRef.detectChanges();
    // Subscribe to toast drop event to clean up
    this.toastRef.instance.toastsDropped.subscribe(() => {
      this.cleanup();
    });
  }

  /**
   * Shows a toast with the specified message, type, and options
   */
  show(
    message: string,
    type: ToastType = 'info',
    options: Partial<Omit<Message, 'id' | 'message' | 'type'>> = {}
  ): string {
    const id = this.generateCryptoId();
    const toast: Message = {
      id,
      message,
      type,
      animation: options.animation ?? this.DEFAULT_TOAST_OPTIONS.animation,
      autohide: options.autohide ?? this.DEFAULT_TOAST_OPTIONS.autohide,
      delay: options.delay ?? this.DEFAULT_TOAST_OPTIONS.delay,
    };

    // Create toast component if it doesn't exist
    if (!this.toastRef) {
      this.create();
    }

    // Add the toast to the component
    if (this.toastRef) {
      this.toastRef.instance.add(toast);
    }

    return id;
  }

  /**
   * Shows a success toast
   */
  success(
    message: string,
    options: Partial<Omit<Message, 'id' | 'message' | 'type'>> = {}
  ): string {
    return this.show(message, 'success', options);
  }

  /**
   * Shows an error toast
   */
  error(message: string, options: Partial<Omit<Message, 'id' | 'message' | 'type'>> = {}): string {
    return this.show(message, 'danger', options);
  }

  /**
   * Shows a warning toast
   */
  warning(
    message: string,
    options: Partial<Omit<Message, 'id' | 'message' | 'type'>> = {}
  ): string {
    return this.show(message, 'warning', options);
  }

  /**
   * Shows an info toast
   */
  info(message: string, options: Partial<Omit<Message, 'id' | 'message' | 'type'>> = {}): string {
    return this.show(message, 'info', options);
  }

  /**
   * Removes a specific toast by ID
   */
  remove(id: string): void {
    if (this.toastRef) {
      this.toastRef.instance.remove(id);
    }
  }

  /**
   * Removes all toasts
   */
  clear(): void {
    if (this.toastRef) {
      this.toastRef.instance.clear();
    }
  }

  /**
   * Cleans up the toast component
   */
  private cleanup(): void {
    if (this.toastRef) {
      this.toastRef.destroy();
      this.toastRef = null;
    }
  }

  /**
   * Generates a cryptographically secure random ID
   */
  private generateCryptoId(): string {
    return crypto.randomUUID();
  }
}

@Directive({
  selector: '[tablerToastInit]',
  standalone: true,
})
export class TablerToastInit implements OnDestroy {
  private readonly el = inject(ElementRef);
  private subscriptions: Subscription[] = [];
  onHidden = output<string>();

  constructor() {
    afterNextRender(async () => {
      try {
        const tabler = await import('@tabler/core');
        const element = this.el.nativeElement;
        const toastInstance = tabler.Toast.getOrCreateInstance(element);

        // Subscribe to hidden event and emit the ID
        const hiddenSubscription = fromEvent(element, 'hidden.bs.toast').subscribe(() => {
          this.onHidden.emit(element.id);
        });
        this.subscriptions.push(hiddenSubscription);
        // Show the toast
        toastInstance.show();
      } catch (error) {
        console.error('Failed to initialize Tabler toast:', error);
      }
    });
  }

  ngOnDestroy(): void {
    // Unsubscribe from all subscriptions to prevent memory leaks
    this.subscriptions.forEach((sub) => sub.unsubscribe());
    this.subscriptions = [];
  }
}

@Component({
  selector: 'tabler-toasts',
  imports: [CommonModule, TablerToastInit],
  template: `
    <div class="toast-container bottom-0 end-0 p-3">
      @for (toast of msgs(); track toast.id) {
      <div
        id="{{ toast.id }}"
        class="toast align-items-center border-0"
        data-bs-toggle="toast"
        role="alert"
        aria-live="assertive"
        [attr.aria-atomic]="toast.animation"
        [attr.data-bs-autohide]="toast.autohide"
        [attr.data-bs-delay]="toast.delay"
        [ngClass]="getToastClass(toast.type)"
        tablerToastInit
      >
        <div class="d-flex">
          <div class="toast-body">
            <span class="me-2">
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
                class="icon icon-2"
              >
                @switch (toast.type) { @case ('success') {
                <path d="M5 12l5 5l10 -10" />
                } @case ('danger') {
                <path d="M3 12a9 9 0 1 0 18 0a9 9 0 0 0 -18 0" />
                <path d="M12 8v4" />
                <path d="M12 16h.01" />
                } @case ('warning') {
                <path d="M12 9v4" />
                <path
                  d="M10.363 3.591l-8.106 13.534a1.914 1.914 0 0 0 1.636 2.871h16.214a1.914 1.914 0 0 0 1.636 -2.87l-8.106 -13.536a1.914 1.914 0 0 0 -3.274 0z"
                />
                <path d="M12 16h.01" />
                } @case ('info') {
                <path d="M3 12a9 9 0 1 0 18 0a9 9 0 0 0 -18 0" />
                <path d="M12 9h.01" />
                <path d="M11 12h1v4h1" />
                } @default {
                <path d="M3 12a9 9 0 1 0 18 0a9 9 0 0 0 -18 0" />
                <path d="M12 9h.01" />
                <path d="M11 12h1v4h1" />
                } }
              </svg>
            </span>
            {{ toast.message }}
          </div>
          <button
            type="button"
            class="btn-close btn-close-white me-2 m-auto"
            data-bs-dismiss="toast"
            (click)="remove(toast.id)"
            aria-label="Close"
          ></button>
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
      .enter-animation {
        animation: slide-fade 3s;
      }
      @keyframes slide-fade {
        from {
          opacity: 0;
          transform: translateY(2rem);
        }
        to {
          opacity: 1;
          transform: translateY(0);
        }
      }
    `,
  ],
  standalone: true,
})
export class Toasts implements OnDestroy {
  msgs = signal<Message[]>([]);
  toastCount = computed(() => this.msgs().length);
  toastsDropped = output<void>();
  private subscriptions: Subscription[] = [];

  /**
   * Adds a toast to the list
   */
  add(toast: Message): void {
    this.msgs.update((list) => [...list, toast]);
  }

  /**
   * Removes a toast by ID
   */
  remove(id: string): void {
    this.msgs.update((list) => list.filter((t) => t.id !== id));
    // Check if all toasts are gone and emit the event if so
    if (this.msgs().length === 0) {
      this.toastsDropped.emit();
    }
  }

  /**
   * Removes all toasts
   */
  clear(): void {
    this.msgs.set([]);
  }

  /**
   * Gets the CSS class for the toast based on its type
   */
  getToastClass(type: ToastType | undefined): string {
    const classMap: Record<ToastType | 'default', string> = {
      success: 'text-bg-success',
      danger: 'text-bg-danger',
      warning: 'text-bg-warning',
      info: 'text-bg-info',
      default: 'text-bg-primary',
    };

    return classMap[type || 'default'];
  }

  ngOnDestroy(): void {
    // Clean up subscriptions to prevent memory leaks
    this.subscriptions.forEach((sub) => sub.unsubscribe());
    this.subscriptions = [];

    // Clear all toasts
    this.msgs.set([]);
  }
}

import { NgComponentOutlet } from '@angular/common';
import {
  afterNextRender,
  ApplicationRef,
  Binding,
  Component,
  ComponentRef,
  createComponent,
  DestroyRef,
  Directive,
  ElementRef,
  EnvironmentInjector,
  inject,
  Service,
  input,
  inputBinding,
  OnDestroy,
  OnInit,
  output,
  signal,
  Type,
} from '@angular/core';
import { outputToObservable, takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { fromEvent } from 'rxjs';
import { ConfirmDialogComponent } from './confirm-dialog';

export interface ModalRef {
  title?: string;
  headerRef?: Type<unknown> | null;
  contentRef?: Type<unknown> | null;
  footerRef?: Type<unknown> | null;
  /** Static inputs forwarded to the content component via NgComponentOutletInputs. */
  contentInputs?: Record<string, unknown>;
  contentBindings?: Binding[];
}

export interface ModalOptions {
  backdrop?: boolean | 'static';
  keyboard?: boolean;
  focus?: boolean;
  show?: boolean;
}

export interface ConfirmOptions {
  title?: string;
  message: string;
  confirmText?: string;
  cancelText?: string;
  danger?: boolean;
}

@Service()
export class ModalsService {
  private appRef = inject(ApplicationRef);
  private injector = inject(EnvironmentInjector);

  private modalRef: ComponentRef<Modals> | null = null;

  create(modalRef: ModalRef) {
    const modalRefSignal = signal(modalRef);
    const bindings = [inputBinding('modalRef', modalRefSignal)];

    this.modalRef = createComponent(Modals, {
      environmentInjector: this.injector,
      bindings: bindings,
    });
    document.body.appendChild(this.modalRef.location.nativeElement);
    this.appRef.attachView(this.modalRef.hostView);
    // 服务方法里没有注入上下文，必须显式传 DestroyRef；取组件自己的，随弹层销毁自动退订
    outputToObservable(this.modalRef.instance.dropped)
      .pipe(takeUntilDestroyed(this.modalRef.injector.get(DestroyRef)))
      .subscribe(() => {
        this.modalRef?.destroy();
        this.modalRef = null;
      });
    return this.modalRef;
  }

  /** Opens a Tabler confirm dialog; resolves `true` on confirm, `false` on cancel/dismiss. */
  confirm(opts: ConfirmOptions): Promise<boolean> {
    return new Promise<boolean>((resolve) => {
      let settled = false;
      const finish = (ok: boolean) => {
        if (settled) {
          return;
        }
        settled = true;
        resolve(ok);
      };
      const ref = this.create({
        title: opts.title ?? '确认操作',
        contentRef: ConfirmDialogComponent,
        contentInputs: {
          message: opts.message,
          confirmText: opts.confirmText ?? '确定',
          cancelText: opts.cancelText ?? '取消',
          danger: opts.danger ?? false,
          onResult: finish,
        },
      });
      // Promise executor 里更没有注入上下文，同样要显式传 DestroyRef
      outputToObservable(ref.instance.dropped)
        .pipe(takeUntilDestroyed(ref.injector.get(DestroyRef)))
        .subscribe(() => finish(false));
    });
  }
}

@Directive({
  selector: '[tablerModalsInit]',
})
export class TablerModalsInit {
  onHidden = output<string>();
  private readonly el = inject(ElementRef);

  constructor() {
    afterNextRender(async () => {
      const ele = this.el.nativeElement;
      const toast = tabler.Modal.getOrCreateInstance(ele);
      ele.addEventListener('hidden.bs.toast', () => this.onHidden.emit(ele.id));
      toast.show();
    });
  }
}

@Component({
  selector: 'tabler-modals',
  imports: [NgComponentOutlet],
  template: `
    <div class="modal" id="exampleModal" tabindex="-1" role="dialog" aria-modal="true">
      <div class="modal-dialog" role="document">
        <div class="modal-content">
          <div class="modal-header">
            @if (modalRef().headerRef) {
              <ng-container *ngComponentOutlet="modalRef().headerRef!" />
            } @else {
              <h5 class="modal-title">{{ modalRef().title }}</h5>
            }
            <button
              type="button"
              class="btn-close"
              data-bs-dismiss="modal"
              aria-label="Close"
            ></button>
          </div>
          @if (modalRef().contentRef) {
            <div class="modal-body">
              <ng-container
                *ngComponentOutlet="modalRef().contentRef!; inputs: modalRef().contentInputs || {}"
              />
            </div>
          }
          @if (modalRef().footerRef) {
            <div class="modal-footer">
              <ng-container *ngComponentOutlet="modalRef().footerRef!" />
            </div>
          }
        </div>
      </div>
    </div>
  `,
  styles: [],
})
export class Modals implements OnInit, OnDestroy {
  private _el = inject(ElementRef);
  private readonly destroyRef = inject(DestroyRef);

  modalRef = input.required<ModalRef>();
  dropped = output<void>();

  constructor() {
    afterNextRender(async () => {
      const options: ModalOptions = {
        backdrop: true,
        keyboard: true,
        focus: true,
        show: true,
      };
      const modalEl = this._el.nativeElement.querySelector('#exampleModal');
      const myModalAlternative = tabler.Modal.getOrCreateInstance(modalEl, options);

      fromEvent(modalEl, 'hidden.bs.modal')
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe(() => {
          this.dropped.emit();
        });
      myModalAlternative.show();
    });
  }

  ngOnInit(): void {}

  ngOnDestroy(): void {}
}

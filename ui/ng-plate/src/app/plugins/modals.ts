import { NgComponentOutlet } from '@angular/common';
import {
  afterNextRender,
  ApplicationRef,
  Binding,
  Component,
  ComponentRef,
  createComponent,
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

export interface ModalRef {
  title?: string;
  headerRef?: Type<unknown> | null;
  contentRef?: Type<unknown> | null;
  footerRef?: Type<unknown> | null;
  contentBindings?: Binding[];
}

export interface ModalOptions {
  backdrop?: boolean | 'static';
  keyboard?: boolean;
  focus?: boolean;
  show?: boolean;
}

@Service()
export class ModalsService {
  private appRef = inject(ApplicationRef);
  private injector = inject(EnvironmentInjector);

  private modalRef: ComponentRef<Modals> | null = null;

  create(modalRef: ModalRef) {
    const modalRefSignal = signal(modalRef);
    const bindings = [inputBinding('modalRef', modalRefSignal)];
    if (modalRef.contentBindings) {
      bindings.push(...modalRef.contentBindings);
    }

    this.modalRef = createComponent(Modals, {
      environmentInjector: this.injector,
      bindings: bindings,
    });
    document.body.appendChild(this.modalRef.location.nativeElement);
    this.appRef.attachView(this.modalRef.hostView);
    outputToObservable(this.modalRef.instance.dropped)
      .pipe(takeUntilDestroyed())
      .subscribe(() => {
        this.modalRef?.destroy();
        this.modalRef = null;
      });
    return this.modalRef;
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
              <ng-container *ngComponentOutlet="modalRef().contentRef!" />
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
        .pipe(takeUntilDestroyed())
        .subscribe(() => {
          this.dropped.emit();
        });
      myModalAlternative.show();
    });
  }

  ngOnInit(): void {}

  ngOnDestroy(): void {}
}

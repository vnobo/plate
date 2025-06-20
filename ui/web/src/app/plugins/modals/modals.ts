import { CommonModule } from '@angular/common';
import {
  afterNextRender,
  ApplicationRef,
  Component,
  ComponentRef,
  createComponent,
  Directive,
  ElementRef,
  EnvironmentInjector,
  inject,
  Injectable,
  input,
  inputBinding,
  OnDestroy,
  OnInit,
  output,
  signal,
  Type,
} from '@angular/core';
import { fromEvent } from 'rxjs';

export interface ModalRef {
  title?: string;
  headerRef?: Type<any> | null;
  contentRef?: Type<any> | null;
  footerRef?: Type<any> | null;
}

export interface ModalOptions {
  backdrop?: boolean | 'static';
  keyboard?: boolean;
  focus?: boolean;
  show?: boolean;
}

@Injectable({ providedIn: 'root' })
export class ModalsService {
  private modalRef: ComponentRef<Modals> | null = null;

  constructor(private appRef: ApplicationRef, private injector: EnvironmentInjector) {}

  create(modalRef: ModalRef) {
    const modalRefSignal = signal(modalRef);
    this.modalRef = createComponent(Modals, {
      environmentInjector: this.injector,
      bindings: [inputBinding('modalRef', modalRefSignal)],
    });
    document.body.appendChild(this.modalRef.location.nativeElement);
    this.appRef.attachView(this.modalRef.hostView);
    this.modalRef.instance.dropped.subscribe(() => {
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
      const tabler = await import('@tabler/core');
      const ele = this.el.nativeElement;
      const toast = tabler.Modal.getOrCreateInstance(ele);
      ele.addEventListener('hidden.bs.toast', () => this.onHidden.emit(ele.id));
      toast.show();
    });
  }
}

@Component({
  selector: 'tabler-modals',
  imports: [CommonModule],
  template: `
    <div class="modal" id="exampleModal" tabindex="-1">
      <div class="modal-dialog" role="document">
        <div class="modal-content">
          <div class="modal-header">
            @if(modalRef().headerRef){
            <ng-container *ngComponentOutlet="modalRef().headerRef!" />
            }@else{ <h5 class="modal-title">{{ modalRef().title }}</h5> }
            <button
              type="button"
              class="btn-close"
              data-bs-dismiss="modal"
              aria-label="Close"></button>
          </div>
          @if(modalRef().contentRef){
          <div class="modal-body">
            <ng-container *ngComponentOutlet="modalRef().contentRef!" />
          </div>
          } @if(modalRef().footerRef){
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
  modalRef = input.required<ModalRef>();
  dropped = output<any>();
  constructor(private _el: ElementRef) {
    afterNextRender(async () => {
      const tabler = await import('@tabler/core');
      const options: ModalOptions = {
        backdrop: true,
        keyboard: true,
        focus: true,
        show: true,
      };
      const modalEl = this._el.nativeElement.querySelector('#exampleModal');
      const myModalAlternative = tabler.Modal.getOrCreateInstance(modalEl, options);

      fromEvent(modalEl, 'hidden.bs.modal').subscribe(($event: any) => {
        console.log('modal closed', $event);
        this.dropped.emit($event);
      });
      myModalAlternative.show();
    });
  }

  ngOnInit(): void {}

  ngOnDestroy(): void {}
}

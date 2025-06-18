import {CommonModule} from '@angular/common';
import {Component, input, Type} from '@angular/core';

export interface Modal {
  title: string;
  contentRef?: Type<any> | null;
  headerRef?: Type<any> | null;
}

export interface ModalOptions {
  backdrop?: boolean | 'static';
  keyboard?: boolean;
  focus?: boolean;
  show?: boolean;
}

@Component({
  selector: 'tabler-modals',
  imports: [CommonModule],
  template: `
    <div class="modal" id="exampleModal" tabindex="-1">
      <div class="modal-dialog" role="document">
        <div class="modal-content">
          @if(modalRef().contentRef){
          <ng-container *ngComponentOutlet="modalRef().contentRef" />
          }@else{
          <div class="modal-header">
            @if(modalRef().headerRef){
            <ng-container *ngComponentOutlet="modalRef().headerRef" />
            }
            <button
              type="button"
              class="btn-close"
              data-bs-dismiss="modal"
              aria-label="Close"></button>
          </div>
          <div class="modal-body">
            Lorem ipsum dolor sit amet, consectetur adipisicing elit. Adipisci animi beatae delectus
            deleniti dolorem eveniet facere fuga iste nemo nesciunt nihil odio perspiciatis, quia
            quis reprehenderit sit tempora totam unde.
          </div>
          <div class="modal-footer">
            <button type="button" class="btn me-auto" data-bs-dismiss="modal">Close</button>
            <button type="button" class="btn btn-primary" data-bs-dismiss="modal"
              >Save changes</button
            >
          </div>
          }
        </div>
      </div>
    </div>
  `,
  styles: [],
})
export class Modals {
  modalRef = input<Modal>();
}

import {CommonModule} from '@angular/common';
import {Component, OnDestroy, OnInit, signal} from '@angular/core';

export type AlertType = 'success' | 'danger' | 'warning' | 'info';

export interface Alert {
  id: string;
  message: string;
  type: AlertType;
}

@Component({
  selector: 'tabler-alert',
  imports: [CommonModule],
  template: `<div
    class="alert alert-dismissible"
    role="alert"
    [ngClass]="{
      'alert-success': alert().type === 'success',
      'alert-danger': alert().type === 'danger',
      'alert-warning': alert().type === 'warning',
      'alert-info': alert().type === 'info',
      'alert-primary': alert().type === undefined
    }">
    <div class="alert-icon">
      @if(alert().type === 'success') {
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
        class="icon  alert-icon icon-2">
        <path d="M5 12l5 5l10 -10" />
      </svg>
      } @else if(alert().type === 'danger') {
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
      } @else if(alert().type === 'warning') {
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
      } @else if(alert().type === 'info') {
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
        class="icon alert-icon icon-2">
        <path d="M3 12a9 9 0 1 0 18 0a9 9 0 0 0 -18 0" />
        <path d="M12 9h.01" />
        <path d="M11 12h1v4h1" />
      </svg>
      }
    </div>
    {{ alert().message }}
    <a class="btn-close" data-bs-dismiss="alert" aria-label="close"></a>
  </div>`,
  styles: `
  `,
})
export class Alerts implements OnInit, OnDestroy {
  alert = signal<Alert>({ id: 'sss', message: 'sss', type: 'info' } as Alert);

  ngOnInit(): void {}

  show(alert: Alert) {
    this.alert.set(alert);
  }

  ngOnDestroy(): void {}
}

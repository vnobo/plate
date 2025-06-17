import {CommonModule} from '@angular/common';
import {Component, OnDestroy, OnInit, signal} from '@angular/core';

export type AlertType = 'success' | 'danger' | 'warning' | 'info';

export interface Alert {
  id: string;
  message: string;
  type: AlertType;
  animation?: boolean;
  autohide?: boolean;
  delay?: number;
}

@Component({
  selector: 'tabler-alert',
  imports: [CommonModule],
  template: `<div class="alert alert-danger alert-dismissible" role="alert">
    <div class="alert-icon">
      <!-- Download SVG icon from http://tabler.io/icons/icon/alert-circle -->
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
    </div>
    <div>This is a danger alert</div>
    <a class="btn-close" data-bs-dismiss="alert" aria-label="close"></a>
  </div>`,
  styles: `
  `,
})
export class Alerts implements OnInit, OnDestroy {
  alerts = signal<Alert[]>([]);
  ngOnInit(): void {}

  ngOnDestroy(): void {}
}

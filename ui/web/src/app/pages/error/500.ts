import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-500-error',
  template: `<div class="page page-center">
    <div class="container-tight py-4">
      <div class="empty">
        <div class="empty-header">500</div>
        <p class="empty-title">Oops… You just found an error page</p>
        <p class="empty-subtitle text-secondary"
          >We are sorry but our server encountered an internal error</p
        >
        <div class="empty-action">
          <a href="/." class="btn btn-primary btn-4">
            <!-- Download SVG icon from http://tabler.io/icons/icon/arrow-left -->
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
              <path d="M5 12l14 0"></path>
              <path d="M5 12l6 6"></path>
              <path d="M5 12l6 -6"></path>
            </svg>
            Take me home
          </a>
        </div>
      </div>
    </div>
  </div>`,
})
export class Ex500 {}

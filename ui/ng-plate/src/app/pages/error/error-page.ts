import { Component, computed, input } from '@angular/core';
import { RouterLink } from '@angular/router';

/** 错误码 → 提示文案，路由 data.type 决定渲染哪一个 */
const MESSAGES: Record<number, string> = {
  403: 'We are sorry but you do not have permission to access this page',
  404: 'We are sorry but the page you are looking for was not found',
  500: 'We are sorry but our server encountered an internal error',
  512: 'We are sorry but your request has been blocked by our server',
};

@Component({
  selector: 'app-error-page',
  imports: [RouterLink],
  template: `
    <div class="page page-center">
      <div class="container-tight py-4">
        <div class="empty">
          <div class="empty-header">{{ message().code }}</div>
          <p class="empty-title">Oops… You just found an error page</p>
          <p class="empty-subtitle text-secondary">{{ message().text }}</p>
          <div class="empty-action">
            <a routerLink="/" class="btn btn-primary">
              <svg
                xmlns="http://www.w3.org/2000/svg"
                class="icon"
                width="24"
                height="24"
                viewBox="0 0 24 24"
                stroke-width="2"
                stroke="currentColor"
                fill="none"
                stroke-linecap="round"
                stroke-linejoin="round"
                aria-hidden="true"
              >
                <path stroke="none" d="M0 0h24v24H0z" fill="none" />
                <path d="M5 12l14 0" />
                <path d="M5 12l6 6" />
                <path d="M5 12l6 -6" />
              </svg>
              Take me home
            </a>
          </div>
        </div>
      </div>
    </div>
  `,
})
export class ErrorPage {
  readonly type = input(500);

  protected readonly message = computed(() => ({
    code: this.type(),
    text: MESSAGES[this.type()] ?? MESSAGES[500],
  }));
}

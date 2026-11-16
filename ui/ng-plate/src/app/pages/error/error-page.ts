import { Component, computed, input } from '@angular/core';
import { RouterLink } from '@angular/router';

import { LocaleSwitcher } from '@app/layout/locale-switcher';

/** Route data.type -> user-facing copy; a switch keeps `$localize` evaluation lazy. */
function messageFor(type: number): string {
  switch (type) {
    case 403:
      return $localize`:@@error403:抱歉，您没有权限访问此页面`;
    case 404:
      return $localize`:@@error404:抱歉，未找到您要访问的页面`;
    case 512:
      return $localize`:@@error512:抱歉，您的请求已被服务器拦截`;
    default:
      return $localize`:@@error500:抱歉，服务器内部出现错误`;
  }
}

@Component({
  selector: 'app-error-page',
  imports: [RouterLink, LocaleSwitcher],
  template: `
    <div class="page page-center">
      <div class="container-tight py-4">
        <div class="d-flex justify-content-end mb-2">
          <app-locale-switcher variant="ghost" />
        </div>
        <div class="empty">
          <div class="empty-header">{{ message().code }}</div>
          <p class="empty-title" i18n="@@errorTitle">哎呀……您遇到了一个错误页面</p>
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
              <span i18n="@@errorBackHome">返回首页</span>
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
    text: messageFor(this.type()),
  }));
}

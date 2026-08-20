import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { LayoutService } from '@app/layout';
import { TokenService } from '@app/core';

@Component({
  selector: 'layout-header',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink],
  template: `
    <div class="navbar navbar-expand-md navbar-dark sticky-top d-print-none">
      <div class="container-xl">
        <button class="navbar-toggler" type="button" aria-label="切换导航" (click)="layout.toggleCollapsed()">
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
          >
            <path stroke="none" d="M0 0h24v24H0z" fill="none" />
            <path d="M4 6l16 0" />
            <path d="M4 12l16 0" />
            <path d="M4 18l16 0" />
          </svg>
        </button>

        <div class="navbar-nav flex-row order-md-last ms-auto">
          <div class="d-none d-md-flex me-2">
            <a routerLink="/dashboard" class="btn btn-ghost-secondary btn-icon" aria-label="返回工作台">
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
              >
                <path stroke="none" d="M0 0h24v24H0z" fill="none" />
                <path d="M5 12l14 0" />
                <path d="M5 12l6 6" />
                <path d="M5 12l6 -6" />
              </svg>
            </a>
          </div>

          <div class="nav-item dropdown">
            <a
              href="javascript:void(0)"
              class="nav-link d-flex lh-1 p-0 px-2"
              data-bs-toggle="dropdown"
              aria-label="打开用户菜单"
            >
              <span class="avatar avatar-sm bg-primary-lt">{{ tokenSer.initial() }}</span>
              <div class="d-none d-xl-block ps-2">
                <div>{{ tokenSer.name() }}</div>
                <div class="mt-1 small text-secondary">{{ tokenSer.role() }}</div>
              </div>
            </a>
            <div class="dropdown-menu dropdown-menu-end dropdown-menu-arrow">
              <a routerLink="/dashboard" class="dropdown-item">工作台</a>
              <div class="dropdown-divider"></div>
              <a href="javascript:void(0)" class="dropdown-item" (click)="logout()">退出登录</a>
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
})
export class LayoutHeader {
  private readonly router = inject(Router);
  protected readonly tokenSer = inject(TokenService);
  readonly layout = inject(LayoutService);

  logout(): void {
    this.tokenSer.logout();
    void this.router.navigate([this.tokenSer.loginUrl]);
  }
}

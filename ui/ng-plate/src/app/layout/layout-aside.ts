import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { LayoutService } from './layout.service';

@Component({
  selector: 'aside[layout-aside]',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, RouterLinkActive],
  host: {
    class: 'navbar navbar-vertical navbar-expand-lg',
    '[class.navbar-folded]': 'layout.collapsed()',
    '[class.show]': 'layout.mobileOpen()',
  },
  template: `
    <div class="container-fluid">
      <button
        class="navbar-brand navbar-brand-autodark w-100 d-none d-lg-flex"
        type="button"
        (click)="layout.toggleCollapsed()"
        [attr.aria-expanded]="!layout.collapsed()"
        aria-label="切换侧边栏"
      >
        <span class="navbar-brand-logo">P</span>
        <span class="navbar-brand-text">Plate</span>
      </button>

      <div class="d-flex d-lg-none align-items-center justify-content-between px-2 py-2 border-bottom">
        <span class="d-flex align-items-center">
          <span class="navbar-brand-logo">P</span>
          <span class="navbar-brand-text">Plate</span>
        </span>
        <button
          class="btn btn-ghost-secondary btn-icon"
          type="button"
          (click)="layout.closeMobile()"
          aria-label="关闭导航"
        >
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
            <path d="M18 6l-12 12" />
            <path d="M6 6l12 12" />
          </svg>
        </button>
      </div>

      <div class="collapse navbar-collapse" id="aside-menu" [class.show]="layout.mobileOpen()">
        <ul class="navbar-nav pt-lg-3">
          @for (item of menu; track item.link) {
            <li class="nav-item">
              <a
                class="nav-link"
                [routerLink]="item.link"
                routerLinkActive="active"
                [routerLinkActiveOptions]="{ exact: item.exact }"
                (click)="layout.closeMobile()"
              >
                <span class="nav-link-icon d-md-none d-lg-inline-block" [innerHTML]="item.icon"></span>
                <span class="nav-link-title">{{ item.title }}</span>
              </a>
            </li>
          }
        </ul>
      </div>
    </div>
  `,
  styles: [
    `
      .navbar-brand-logo {
        display: inline-flex;
        align-items: center;
        justify-content: center;
        width: 1.75rem;
        height: 1.75rem;
        margin-right: 0.5rem;
        border-radius: 0.375rem;
        background: var(--tblr-primary, #066fd1);
        color: #fff;
        font-weight: 700;
      }

      :host(.navbar-folded) .navbar-brand-text {
        display: none;
      }

      /* Mobile: off-canvas drawer instead of an empty in-flow bar */
      @media (max-width: 991.98px) {
        :host {
          position: fixed;
          inset-block: 0;
          inset-inline-start: 0;
          width: 16rem;
          max-width: 85vw;
          z-index: 1045;
          transform: translateX(-100%);
          transition: transform 0.3s ease;
          visibility: hidden;
        }

        :host(.show) {
          transform: translateX(0);
          visibility: visible;
        }

        :host .container-fluid {
          flex-direction: column;
          flex-wrap: nowrap;
          justify-content: flex-start;
          align-content: flex-start;
          height: 100%;
        }
      }
    `,
  ],
})
export class LayoutAside {
  readonly layout = inject(LayoutService);

  readonly menu: ReadonlyArray<{ title: string; link: string; icon: string; exact: boolean }> = [
    {
      title: '欢迎',
      link: '/platform/users',
      exact: false,
      icon: '<svg xmlns="http://www.w3.org/2000/svg" class="icon" width="24" height="24" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"/><path d="M5 12l-2 0l9 -9l9 9l-2 0" /><path d="M5 12v7a2 2 0 0 0 2 2h10a2 2 0 0 0 2 -2v-7" /><path d="M9 21v-6a2 2 0 0 1 2 -2h2a2 2 0 0 1 2 2v6" /></svg>',
    },
    {
      title: '租户管理',
      link: '/platform/tenant',
      exact: false,
      icon: '<svg xmlns="http://www.w3.org/2000/svg" class="icon" width="24" height="24" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"/><path d="M3 21l18 0" /><path d="M3 10l18 0" /><path d="M5 6l7 -3l7 3" /><path d="M4 10l0 11" /><path d="M20 10l0 11" /><path d="M8 14l0 3" /><path d="M12 14l0 3" /><path d="M16 14l0 3" /></svg>',
    },
    {
      title: '角色管理',
      link: '/platform/role',
      exact: false,
      icon: '<svg xmlns="http://www.w3.org/2000/svg" class="icon" width="24" height="24" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"/><path d="M9 7m-4 0a4 4 0 1 0 8 0a4 4 0 1 0 -8 0" /><path d="M3 21v-2a4 4 0 0 1 4 -4h4a4 4 0 0 1 4 4v2" /><path d="M16 3.13a4 4 0 0 1 0 7.75" /><path d="M21 21v-2a4 4 0 0 0 -3 -3.85" /></svg>',
    },
  ];
}

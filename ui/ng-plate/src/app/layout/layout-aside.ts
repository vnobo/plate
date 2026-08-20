import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { LayoutService } from '@app/layout';

@Component({
  selector: 'layout-aside',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, RouterLinkActive],
  template: `
    <aside class="navbar navbar-vertical navbar-expand-lg" [class.navbar-collapsed]="layout.collapsed()">
      <div class="container-fluid">
        <button
          class="navbar-brand navbar-brand-autodark w-100 d-none d-lg-flex"
          type="button"
          (click)="layout.toggleCollapsed()"
          aria-label="切换侧边栏"
        >
          <span class="navbar-brand-logo">P</span>
          <span class="navbar-brand-text">Plate</span>
        </button>

        <div class="collapse navbar-collapse" id="aside-menu">
          <ul class="navbar-nav pt-lg-3">
            @for (item of menu; track item.link) {
              <li class="nav-item">
                <a
                  class="nav-link"
                  [routerLink]="item.link"
                  routerLinkActive="active"
                  [routerLinkActiveOptions]="{ exact: item.exact }"
                >
                  <span class="nav-link-icon d-md-none d-lg-inline-block" [innerHTML]="item.icon"></span>
                  <span class="nav-link-title">{{ item.title }}</span>
                </a>
              </li>
            }
          </ul>
        </div>
      </div>
    </aside>
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

      .navbar-collapsed .navbar-brand-text {
        display: none;
      }
    `,
  ],
})
export class LayoutAside {
  readonly layout = inject(LayoutService);

  readonly menu: ReadonlyArray<{ title: string; link: string; icon: string; exact: boolean }> = [
    {
      title: '欢迎',
      link: 'users',
      exact: false,
      icon: '<svg xmlns="http://www.w3.org/2000/svg" class="icon" width="24" height="24" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"/><path d="M5 12l-2 0l9 -9l9 9l-2 0" /><path d="M5 12v7a2 2 0 0 0 2 2h10a2 2 0 0 0 2 -2v-7" /><path d="M9 21v-6a2 2 0 0 1 2 -2h2a2 2 0 0 1 2 2v6" /></svg>',
    },
    {
      title: '租户管理',
      link: 'tenant',
      exact: false,
      icon: '<svg xmlns="http://www.w3.org/2000/svg" class="icon" width="24" height="24" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"/><path d="M3 21l18 0" /><path d="M3 10l18 0" /><path d="M5 6l7 -3l7 3" /><path d="M4 10l0 11" /><path d="M20 10l0 11" /><path d="M8 14l0 3" /><path d="M12 14l0 3" /><path d="M16 14l0 3" /></svg>',
    },
    {
      title: '角色管理',
      link: 'role',
      exact: false,
      icon: '<svg xmlns="http://www.w3.org/2000/svg" class="icon" width="24" height="24" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"/><path d="M9 7m-4 0a4 4 0 1 0 8 0a4 4 0 1 0 -8 0" /><path d="M3 21v-2a4 4 0 0 1 4 -4h4a4 4 0 0 1 4 4v2" /><path d="M16 3.13a4 4 0 0 1 0 7.75" /><path d="M21 21v-2a4 4 0 0 0 -3 -3.85" /></svg>',
    },
  ];
}

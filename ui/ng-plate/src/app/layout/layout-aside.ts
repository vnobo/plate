import { httpResource } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, computed, inject } from '@angular/core';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { RouterLink, RouterLinkActive } from '@angular/router';

import { TokenService } from '@app/core';
import { environment } from '@envs/env';
import { LayoutService } from './layout.service';

/** 后端 se_menus 的菜单项，只声明侧栏用到的字段 */
interface MenuItem {
  code?: string;
  name?: string;
  path?: string;
  /** FOLDER 是分组目录，只有它下面挂 MENU；MENU/LINK/API 才是可点的项 */
  type?: 'FOLDER' | 'MENU' | 'LINK' | 'API';
  icons?: string;
}

interface NavItem {
  title: string;
  link: string;
  icon: SafeHtml;
  /** FOLDER 只作分组标题，不参与导航 */
  heading: boolean;
}

const svg = (body: string) =>
  `<svg xmlns="http://www.w3.org/2000/svg" class="icon" width="24" height="24" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round">${body}</svg>`;

/**
 * 后端 icons 字段给的是图标名（setting/user/group/menu），项目没装图标库，
 * 所以按名字映射到内联 SVG；认不出的名字走 FALLBACK，避免图标位空白。
 */
const ICONS: Record<string, string> = {
  setting: svg(
    '<path d="M10.325 4.317c.426 -1.756 2.924 -1.756 3.35 0a1.724 1.724 0 0 0 2.573 1.066c1.543 -.94 3.31 .826 2.37 2.37a1.724 1.724 0 0 0 1.065 2.572c1.756 .426 1.756 2.924 0 3.35a1.724 1.724 0 0 0 -1.066 2.573c.94 1.543 -.826 3.31 -2.37 2.37a1.724 1.724 0 0 0 -2.572 1.065c-.426 1.756 -2.924 1.756 -3.35 0a1.724 1.724 0 0 0 -2.573 -1.066c-1.543 .94 -3.31 -.826 -2.37 -2.37a1.724 1.724 0 0 0 -1.065 -2.572c-1.756 -.426 -1.756 -2.924 0 -3.35a1.724 1.724 0 0 0 1.066 -2.573c-.94 -1.543 .826 -3.31 2.37 -2.37c1 .608 2.296 .07 2.572 -1.065z" /><path d="M9 12a3 3 0 1 0 6 0a3 3 0 0 0 -6 0" />',
  ),
  user: svg(
    '<path d="M9 7m-4 0a4 4 0 1 0 8 0a4 4 0 1 0 -8 0" /><path d="M3 21v-2a4 4 0 0 1 4 -4h4a4 4 0 0 1 4 4v2" />',
  ),
  group: svg(
    '<path d="M9 7m-4 0a4 4 0 1 0 8 0a4 4 0 1 0 -8 0" /><path d="M3 21v-2a4 4 0 0 1 4 -4h4a4 4 0 0 1 4 4v2" /><path d="M16 3.13a4 4 0 0 1 0 7.75" /><path d="M21 21v-2a4 4 0 0 0 -3 -3.85" />',
  ),
  menu: svg('<path d="M4 6l16 0" /><path d="M4 12l16 0" /><path d="M4 18l16 0" />'),
  home: svg(
    '<path d="M5 12l-2 0l9 -9l9 9l-2 0" /><path d="M5 12v7a2 2 0 0 0 2 2h10a2 2 0 0 0 2 -2v-7" />',
  ),
  building: svg(
    '<path d="M3 21l18 0" /><path d="M3 10l18 0" /><path d="M5 6l7 -3l7 3" /><path d="M4 10l0 11" /><path d="M20 10l0 11" /><path d="M8 14l0 3" /><path d="M12 14l0 3" /><path d="M16 14l0 3" />',
  ),
};

const FALLBACK_ICON = svg(
  '<path d="M5 12m-1 0a1 1 0 1 0 2 0a1 1 0 1 0 -2 0" /><path d="M12 12m-1 0a1 1 0 1 0 2 0a1 1 0 1 0 -2 0" /><path d="M19 12m-1 0a1 1 0 1 0 2 0a1 1 0 1 0 -2 0" />',
);

@Component({
  selector: 'aside[layout-aside]',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, RouterLinkActive],
  host: {
    class: 'navbar navbar-vertical navbar-expand-lg',
    '[class.navbar-folded]': 'layout.collapsed()',
    '[class.show]': 'layout.mobileOpen()',
    '[attr.aria-label]': '"主导航"',
    // 抽屉打开时按 Esc 关闭（closeMobile 幂等，未打开时无副作用）
    '(document:keydown.escape)': 'layout.closeMobile()',
  },
  template: `
    <div class="container-fluid">
      <!-- 桌面端品牌区：纯链接，回到工作台（折叠按钮改放到顶栏） -->
      <div class="navbar-brand navbar-brand-autodark d-none d-lg-flex">
        <a class="d-flex align-items-center text-decoration-none" routerLink="/dashboard">
          <span class="navbar-brand-logo">P</span>
          <span class="navbar-brand-text">Plate</span>
        </a>
      </div>

      <!-- 移动端抽屉头部 -->
      <div
        class="d-flex d-lg-none align-items-center justify-content-between px-3 py-2 border-bottom"
      >
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
          @for (item of menu(); track item.link) {
            @if (item.heading) {
              <!-- FOLDER 只是分组标题，点了没有落地页，所以不给链接 -->
              <li class="nav-item">
                <div class="nav-link disabled">{{ item.title }}</div>
              </li>
            } @else {
              <!-- active 类要落在 nav-item 上：Tabler 用 .nav-item.active > .nav-link 做激活态 -->
              <li class="nav-item" routerLinkActive="active" ariaCurrentWhenActive="page">
                <a class="nav-link" [routerLink]="item.link" (click)="layout.closeMobile()">
                  <span
                    class="nav-link-icon d-md-none d-lg-inline-block"
                    [innerHTML]="item.icon"
                  ></span>
                  <span class="nav-link-title">{{ item.title }}</span>
                </a>
              </li>
            }
          }
          @if (menuFailed()) {
            <li class="nav-item">
              <button class="nav-link text-danger" type="button" (click)="reloadMenu()">
                菜单加载失败，点击重试
              </button>
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

      .navbar-brand-text {
        font-weight: 600;
        letter-spacing: 0.01em;
      }

      :host(.navbar-folded) .navbar-brand-text {
        display: none;
      }

      /* 折叠态下图标居中 */
      :host(.navbar-folded) .nav-link {
        justify-content: center;
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
  // 图标是硬编码的静态 SVG；Angular 的 innerHTML 清洗会移除 <svg>，故声明为可信内容
  private readonly sanitizer = inject(DomSanitizer);
  private readonly tokenSer = inject(TokenService);

  /**
   * 当前用户的菜单。httpResource 是 HttpClient 的响应式封装，全局拦截器（令牌、401 登出）照旧生效；
   * 未登录（含 SSR 首屏没有会话）时返回 undefined，资源保持 idle 不发请求。
   */
  protected readonly menuResource = httpResource<MenuItem[]>(
    () => (this.tokenSer.isLogged() ? { url: environment.relaApiPath + '/menus/me' } : undefined),
    { defaultValue: [] },
  );

  /** 401 由全局拦截器统一登出并跳登录页，这里只兜住「请求失败/超时」这类情况 */
  protected readonly menuFailed = computed(() => !!this.menuResource.error());

  protected readonly menu = computed<NavItem[]>(() => {
    // 错误态读 value() 会抛错，先问 hasValue()（官方明确要求）
    if (!this.menuResource.hasValue()) return [];
    return this.menuResource.value().map((item) => ({
      title: item.name ?? item.path ?? '',
      // 后端 path 是模块内的相对路径，前端统一挂在 /platform 下
      link: `/platform/${item.path ?? ''}`,
      icon: this.sanitizer.bypassSecurityTrustHtml(ICONS[item.icons ?? ''] ?? FALLBACK_ICON),
      heading: item.type === 'FOLDER',
    }));
  });

  protected reloadMenu(): void {
    this.menuResource.reload();
  }
}

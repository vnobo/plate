import {
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  computed,
  inject,
  signal,
  viewChild,
} from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { SettingsService, TokenService, UserService } from '@app/core';
import { LayoutService } from './layout.service';

type NoticeType = 'system' | 'mention' | 'comment' | 'like';

interface Notice {
  id: number;
  type: NoticeType;
  title: string;
  text: string;
  time: string;
  /** Timestamp used for sorting only */
  at: number;
  unread: boolean;
  starred: boolean;
}

/** Notice type → icon / dot colour / label. Dot colour encodes the type, animation = unread, grey = read */
const NOTICE_TYPES: Record<NoticeType, { label: string; color: string; icon: string }> = {
  system: {
    label: 'System',
    color: 'red',
    icon: '<svg class="icon icon-sm text-secondary" xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"/><path d="M3 12a9 9 0 1 0 18 0a9 9 0 0 0 -18 0" /><path d="M12 9h.01" /><path d="M11 12h1v4h1" /></svg>',
  },
  mention: {
    label: '@Mentions',
    color: 'blue',
    icon: '<svg class="icon icon-sm text-secondary" xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"/><path d="M12 12m-4 0a4 4 0 1 0 8 0a4 4 0 1 0 -8 0" /><path d="M16 12v1.5a2.5 2.5 0 0 0 5 0v-1.5a9 9 0 1 0 -5.5 8.28" /></svg>',
  },
  comment: {
    label: 'Comments',
    color: 'green',
    icon: '<svg class="icon icon-sm text-secondary" xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"/><path d="M8 9h8" /><path d="M8 13h6" /><path d="M18 4a3 3 0 0 1 3 3v8a3 3 0 0 1 -3 3h-5l-5 3v-3h-2a3 3 0 0 1 -3 -3v-8a3 3 0 0 1 3 -3h12z" /></svg>',
  },
  like: {
    label: 'Likes',
    color: 'pink',
    icon: '<svg class="icon icon-sm text-secondary" xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"/><path d="M19.5 12.572l-7.5 7.428l-7.5 -7.428a5 5 0 1 1 7.5 -6.566a5 5 0 1 1 7.5 6.566z" /></svg>',
  },
};

/** Sort priority: System > @Mentions > Comments > Likes */
const TYPE_PRIORITY: NoticeType[] = ['system', 'mention', 'comment', 'like'];

@Component({
  selector: 'header[layout-header]',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, RouterLinkActive],
  host: {
    // Align with the sidebar breakpoint using expand-lg: otherwise between 48rem–61.9375rem the hamburger is hidden while the sidebar is still a drawer, leaving navigation unreachable
    class: 'navbar navbar-expand-lg sticky-top d-print-none',
    '[attr.aria-label]': '"Top navigation"',
  },
  template: `
    <!-- flex-nowrap: on narrow screens the navbar's default flex-wrap pushes the action area onto a second row; the module nav stays visible so one row must be kept -->
    <div class="container-xl flex-nowrap">
      <!-- Mobile: open the sidebar drawer -->
      <button
        class="navbar-toggler d-lg-none"
        type="button"
        (click)="layout.toggleMobile()"
        [attr.aria-expanded]="layout.mobileOpen()"
        aria-controls="aside-menu"
        aria-label="Open navigation"
      >
        <!-- Tabler's built-in toggler icon (1.5em, drawn with mask, same as the preview page); an inline svg.icon would be squeezed to 0.5rem wide by the .icon rule -->
        <span class="navbar-toggler-icon"></span>
      </button>

      <!-- Desktop: collapse / expand the sidebar -->
      <button
        class="btn btn-ghost-secondary btn-icon d-none d-lg-flex"
        type="button"
        (click)="layout.toggleCollapsed()"
        [attr.aria-expanded]="!layout.collapsed()"
        aria-controls="aside-menu"
        [attr.aria-label]="layout.collapsed() ? 'Expand sidebar' : 'Collapse sidebar'"
      >
        @if (layout.collapsed()) {
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
            <path d="M7 7l5 5l-5 5" />
            <path d="M13 7l5 5l-5 5" />
          </svg>
        } @else {
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
            <path d="M17 7l-5 5l5 5" />
          </svg>
        }
      </button>

      <!-- Module primary nav: Tabler horizontal nav navbar-nav > nav-item > nav-link;
           flex-row keeps it horizontal on narrow screens, otherwise .navbar-nav defaults to a vertical full-width row;
           the project does not define --tblr-navbar-active-color, so .active alone shows no highlight, hence the primary colour and medium font weight -->
      <ul class="navbar-nav flex-row ms-2">
        <li class="nav-item">
          <a
            class="nav-link px-2"
            routerLink="/platform"
            routerLinkActive="active text-primary fw-medium"
            ariaCurrentWhenActive="page"
            >Platform</a
          >
        </li>
        <li class="nav-item">
          <a
            class="nav-link px-2"
            routerLink="/dashboard"
            routerLinkActive="active text-primary fw-medium"
            ariaCurrentWhenActive="page"
            >Dashboard</a
          >
        </li>
        <li class="nav-item">
          <a
            class="nav-link px-2"
            routerLink="/examples"
            routerLinkActive="active text-primary fw-medium"
            ariaCurrentWhenActive="page"
            >Examples</a
          >
        </li>
      </ul>

      <!-- Action area: order follows the Tabler example (theme → notifications → language → user), all styled uniformly with .nav-link -->
      <div class="navbar-nav flex-row order-md-last ms-auto align-items-center">
        <!-- Light/dark toggle: which button is shown is decided by Tabler's .hide-theme-dark / .hide-theme-light
             based on data-bs-theme, so the component does not need to track the current theme -->
        <div class="nav-item d-flex">
          <a
            href="javascript:void(0)"
            class="nav-link px-2 hide-theme-dark"
            (click)="settings.setTheme('dark')"
            title="Switch to dark mode"
            aria-label="Switch to dark mode"
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
              <path
                d="M12 3c.132 0 .263 0 .393 0a7.5 7.5 0 0 0 7.92 12.446a9 9 0 1 1 -8.313 -12.454z"
              />
            </svg>
          </a>
          <a
            href="javascript:void(0)"
            class="nav-link px-2 hide-theme-light"
            (click)="settings.setTheme('light')"
            title="Switch to light mode"
            aria-label="Switch to light mode"
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
              <path d="M12 12m-4 0a4 4 0 1 0 8 0a4 4 0 1 0 -8 0" />
              <path
                d="M3 12h1m8 -9v1m8 8h1m-9 8v1m-6.4 -15.4l.7 .7m12.1 -.7l-.7 .7m0 11.4l.7 .7m-12.1 -.7l-.7 .7"
              />
            </svg>
          </a>
        </div>

        <!-- Notifications: the red dot relies on the .nav-link > .badge absolute-positioning rule, so it cannot be replaced by btn -->
        <div class="nav-item dropdown">
          <a
            #notifyToggle
            href="javascript:void(0)"
            class="nav-link px-2"
            data-bs-toggle="dropdown"
            data-bs-auto-close="outside"
            aria-label="Notifications"
            (click)="openNotifications()"
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
              <path
                d="M10 5a2 2 0 1 1 4 0a7 7 0 0 1 4 6v3a4 4 0 0 0 2 3h-16a4 4 0 0 0 2 -3v-3a7 7 0 0 1 4 -6"
              />
              <path d="M9 17v1a3 3 0 0 0 6 0v-1" />
            </svg>
            @if (unreadCount() > 0) {
              <span class="badge bg-red"></span>
            }
          </a>

          <div class="dropdown-menu dropdown-menu-arrow dropdown-menu-end dropdown-menu-card">
            <div class="card">
              <div class="card-header d-flex align-items-center">
                <h2 class="card-title">Notifications</h2>
                <span class="ms-2 small text-secondary">{{ unreadCount() }} unread</span>
                <button
                  type="button"
                  class="btn-close ms-auto"
                  (click)="closeNotifications()"
                  aria-label="Close"
                ></button>
              </div>

              <div class="list-group list-group-flush list-group-hoverable">
                @if (loading()) {
                  @for (i of skeletons; track i) {
                    <div class="list-group-item">
                      <div class="row align-items-center placeholder-glow">
                        <div class="col-auto">
                          <span class="status-dot d-block"></span>
                        </div>
                        <div class="col">
                          <span class="placeholder col-6 d-block mb-1"></span>
                          <span class="placeholder col-10 d-block"></span>
                        </div>
                      </div>
                    </div>
                  }
                } @else {
                  @for (item of list(); track item.id) {
                    <div class="list-group-item">
                      <div class="row align-items-center">
                        <div class="col-auto">
                          <span
                            [class]="
                              'status-dot d-block' +
                              (item.unread ? ' status-dot-animated bg-' + colorOf(item.type) : '')
                            "
                          >
                            <span class="visually-hidden">{{ item.unread ? 'Unread' : 'Read' }}</span>
                          </span>
                        </div>
                        <div class="col text-truncate">
                          <div class="d-flex align-items-center">
                            <span class="me-1 d-inline-flex" [innerHTML]="iconOf(item.type)"></span>
                            <a
                              href="javascript:void(0)"
                              class="text-body d-block text-truncate"
                              [attr.title]="item.title"
                              (click)="markRead(item.id)"
                            >
                              {{ item.title }}
                            </a>
                          </div>
                          <div class="d-flex align-items-center mt-n1">
                            <div class="text-secondary text-truncate" [attr.title]="item.text">
                              {{ item.text }}
                            </div>
                            <span class="ms-auto ps-2 small text-secondary text-nowrap">{{
                              item.time
                            }}</span>
                          </div>
                        </div>
                        <div class="col-auto">
                          <a
                            href="javascript:void(0)"
                            class="list-group-item-actions"
                            [class.show]="item.starred"
                            [attr.aria-label]="item.starred ? 'Unstar' : 'Star'"
                            (click)="toggleStar(item.id)"
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
                              [class.text-yellow]="item.starred"
                              [class.text-muted]="!item.starred"
                            >
                              <path stroke="none" d="M0 0h24v24H0z" fill="none" />
                              <path
                                d="M12 17.75l-6.172 3.245l1.179 -6.873l-5 -4.867l6.9 -1l3.086 -6.253l3.086 6.253l6.9 1l-5 4.867l1.179 6.873l-6.158 -3.245"
                              />
                            </svg>
                          </a>
                        </div>
                      </div>
                    </div>
                  } @empty {
                    <div class="list-group-item">
                      <div class="text-center text-secondary py-4">
                        <svg
                          xmlns="http://www.w3.org/2000/svg"
                          class="icon icon-lg mb-2"
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
                          <path
                            d="M4 4m0 2a2 2 0 0 1 2 -2h12a2 2 0 0 1 2 2v12a2 2 0 0 1 -2 2h-12a2 2 0 0 1 -2 -2z"
                          />
                          <path d="M4 13h3l3 3h4l3 -3h3" />
                        </svg>
                        <div>No notifications</div>
                      </div>
                    </div>
                  }
                }
              </div>

              <div class="card-body">
                <div class="row g-2">
                  <div class="col">
                    <button class="btn w-100" type="button" (click)="archiveAll()">Archive all</button>
                  </div>
                  <div class="col">
                    <button
                      class="btn w-100"
                      type="button"
                      [disabled]="unreadCount() === 0"
                      (click)="markAllRead()"
                    >
                      Mark all as read
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Language: shortcut: records only the preference and <html lang>; the project has no i18n resources yet, and copy will be driven by them once integrated -->
        <div class="nav-item dropdown d-none d-md-flex">
          <a
            href="javascript:void(0)"
            class="nav-link px-2 d-flex align-items-center"
            data-bs-toggle="dropdown"
            aria-label="Switch language"
          >
            <svg
              xmlns="http://www.w3.org/2000/svg"
              class="icon me-1"
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
              <path d="M3 12a9 9 0 1 0 18 0a9 9 0 0 0 -18 0" />
              <path d="M3.6 9h16.8" />
              <path d="M3.6 15h16.8" />
              <path d="M11.5 3a17 17 0 0 0 0 18" />
              <path d="M12.5 3a17 17 0 0 1 0 18" />
            </svg>
            {{ langLabel() }}
          </a>
          <div class="dropdown-menu dropdown-menu-end">
            <button class="dropdown-item" type="button" (click)="setLang('zh-CN')">Simplified Chinese</button>
            <button class="dropdown-item" type="button" (click)="setLang('en')">English</button>
          </div>
        </div>

        <div class="nav-item dropdown">
          <a
            href="javascript:void(0)"
            class="nav-link d-flex lh-1 p-0 px-2"
            data-bs-toggle="dropdown"
            aria-label="Open user menu"
          >
            <span class="avatar avatar-sm bg-primary-lt">{{ userSer.initial() }}</span>
            <!-- Show only the name and nickname; roles/permissions are not exposed in the UI -->
            <div class="d-none d-xl-block ps-2 lh-sm">
              <div class="fw-medium">{{ userSer.displayName() }}</div>
              @if (showNickname()) {
                <div class="small text-secondary">{{ userSer.nickname() }}</div>
              }
            </div>
          </a>
          <div class="dropdown-menu dropdown-menu-end dropdown-menu-arrow">
            <!-- Below xl the top bar keeps only the avatar, so the name moves into the menu; otherwise small screens cannot tell who is signed in -->
            <div class="d-xl-none">
              <div class="dropdown-item-text d-flex align-items-center">
                <span class="avatar avatar-sm bg-primary-lt">{{ userSer.initial() }}</span>
                <div class="ps-2 lh-sm">
                  <div class="fw-medium">{{ userSer.displayName() }}</div>
                  @if (showNickname()) {
                    <div class="small text-secondary">{{ userSer.nickname() }}</div>
                  }
                </div>
              </div>
              <div class="dropdown-divider"></div>
            </div>
            <a routerLink="/dashboard" class="dropdown-item">
              <svg
                xmlns="http://www.w3.org/2000/svg"
                class="icon dropdown-item-icon"
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
                <path
                  d="M10 3.2a9 9 0 1 0 10.8 10.8a1 1 0 0 0 -1 -1h-6.8a2 2 0 0 1 -2 -2v-7a.9 .9 0 0 0 -1 -.8"
                />
                <path d="M15 3.5a9 9 0 0 1 5.5 5.5h-4.5a1 1 0 0 1 -1 -1v-4.5" />
              </svg>
              Dashboard
            </a>
            <div class="dropdown-divider"></div>
            <a href="javascript:void(0)" class="dropdown-item" (click)="logout()">
              <svg
                xmlns="http://www.w3.org/2000/svg"
                class="icon dropdown-item-icon"
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
                <path
                  d="M14 8v-2a2 2 0 0 0 -2 -2h-7a2 2 0 0 0 -2 2v12a2 2 0 0 0 2 2h7a2 2 0 0 0 2 -2v-2"
                />
                <path d="M7 12h14l-3 -3" />
                <path d="M17 9l3 3" />
              </svg>
              Log out
            </a>
          </div>
        </div>
      </div>
    </div>
  `,
})
export class LayoutHeader {
  private readonly router = inject(Router);
  protected readonly tokenSer = inject(TokenService);
  protected readonly userSer: UserService = inject(UserService);
  protected readonly settings = inject(SettingsService);
  protected readonly layout = inject(LayoutService);
  private readonly http = inject(HttpClient);

  /** Show one line when the nickname matches the display name, avoiding a duplicate like "Zhang San / Zhang San" */
  protected readonly showNickname = computed(() => {
    const nickname = this.userSer.nickname();
    return nickname !== '' && nickname !== this.userSer.displayName();
  });

  // Icons are hard-coded static SVG; Angular's innerHTML sanitisation strips <svg>, so they are declared trusted
  private readonly sanitizer = inject(DomSanitizer);
  private readonly typeIcons = new Map<NoticeType, SafeHtml>(
    (Object.keys(NOTICE_TYPES) as NoticeType[]).map((type) => [
      type,
      this.sanitizer.bypassSecurityTrustHtml(NOTICE_TYPES[type].icon),
    ]),
  );

  private readonly notifyToggle = viewChild.required<ElementRef<HTMLElement>>('notifyToggle');

  /** Sample notice data: no back-end endpoint yet, used to verify structure and interactions */
  protected readonly notices = signal<ReadonlyArray<Notice>>([
    {
      id: 1,
      type: 'system',
      title: 'Version 1.4.0 released',
      text: 'Fixed permissions not refreshing after switching tenants (#29604)',
      time: '10 minutes ago',
      at: Date.now() - 10 * 60_000,
      unread: true,
      starred: false,
    },
    {
      id: 2,
      type: 'mention',
      title: 'Zhang Wei mentioned you in "User Management"',
      text: 'Please confirm whether the group permissions for this tenant are correct (#29734)',
      time: '1 hour ago',
      at: Date.now() - 60 * 60_000,
      unread: true,
      starred: true,
    },
    {
      id: 3,
      type: 'comment',
      title: 'Li Na commented on your role configuration',
      text: 'Suggest splitting the "Read-only" role into two finer-grained permissions (#29736)',
      time: '3 hours ago',
      at: Date.now() - 3 * 60 * 60_000,
      unread: false,
      starred: false,
    },
    {
      id: 4,
      type: 'like',
      title: 'Wang Qiang liked your monthly report',
      text: 'The "Monthly Active Users" report received 5 likes (#29730)',
      time: 'Yesterday 18:02',
      at: Date.now() - 26 * 60 * 60_000,
      unread: true,
      starred: false,
    },
  ]);

  /** Sort: unread first → type priority (System > @Mentions > Comments > Likes) → newest first */
  protected readonly list = computed(() => {
    const priority = TYPE_PRIORITY;
    return [...this.notices()].sort(
      (a, b) =>
        Number(b.unread) - Number(a.unread) ||
        priority.indexOf(a.type) - priority.indexOf(b.type) ||
        b.at - a.at,
    );
  });

  protected readonly unreadCount = computed(() => this.notices().filter((n) => n.unread).length);
  protected readonly loading = signal(false);
  protected readonly skeletons = [1, 2, 3];
  private loaded = false;

  protected readonly lang = signal<'zh-CN' | 'en'>(
    typeof localStorage !== 'undefined' && localStorage.getItem('plate-lang') === 'en'
      ? 'en'
      : 'zh-CN',
  );
  protected readonly langLabel = computed(() => (this.lang() === 'en' ? 'EN' : 'Chinese'));

  colorOf(type: NoticeType): string {
    return NOTICE_TYPES[type].color;
  }

  iconOf(type: NoticeType): SafeHtml {
    return this.typeIcons.get(type)!;
  }

  /** Load on first open; shortcut: no notification endpoint yet, a 400ms delay simulates a fetch, replace with a real request once integrated */
  openNotifications(): void {
    if (this.loaded) return;
    this.loaded = true;
    this.loading.set(true);
    setTimeout(() => this.loading.set(false), 400);
  }

  markRead(id: number): void {
    this.notices.update((list) => list.map((n) => (n.id === id ? { ...n, unread: false } : n)));
  }

  toggleStar(id: number): void {
    this.notices.update((list) =>
      list.map((n) => (n.id === id ? { ...n, starred: !n.starred } : n)),
    );
  }

  markAllRead(): void {
    this.notices.update((list) => list.map((n) => ({ ...n, unread: false })));
  }

  archiveAll(): void {
    this.notices.set([]);
  }

  /** Closing the panel uses Tabler's Dropdown instance (Bootstrap 5's dropdown does not support data-bs-dismiss) */
  closeNotifications(): void {
    const dropdown = (
      window as unknown as {
        tabler?: { Dropdown?: { getOrCreateInstance(el: Element): { hide(): void } } };
      }
    ).tabler?.Dropdown;
    dropdown?.getOrCreateInstance(this.notifyToggle().nativeElement).hide();
  }

  setLang(lang: 'zh-CN' | 'en'): void {
    this.lang.set(lang);
    localStorage.setItem('plate-lang', lang);
    document.documentElement.lang = lang;
  }

  logout(): void {
    // Explicit logout: first ask the back-end to revoke the HttpOnly remember-me cookie (the front-end cannot read or delete it),
    // and clear local state regardless of whether the server succeeds, otherwise the user stays signed in after clicking logout
    this.http.get('/oauth2/logout').subscribe({
      next: () => this.finishLogout(),
      error: () => this.finishLogout(),
    });
  }

  private finishLogout() {
    this.tokenSer.logout();
    void this.router.navigate([this.tokenSer.loginUrl]);
  }
}

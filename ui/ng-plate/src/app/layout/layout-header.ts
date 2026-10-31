import {
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  computed,
  inject,
  signal,
  viewChild,
} from '@angular/core';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { Router, RouterLink } from '@angular/router';
import { SettingsService, TokenService, UserService } from '@app/core';
import { LayoutService } from './layout.service';

type NoticeType = 'system' | 'mention' | 'comment' | 'like';

interface Notice {
  id: number;
  type: NoticeType;
  title: string;
  text: string;
  time: string;
  /** 仅用于排序的时间戳 */
  at: number;
  unread: boolean;
  starred: boolean;
}

/** 通知类型 → 图标/圆点配色/文案。圆点颜色即类型，动画=未读，灰色=已读 */
const NOTICE_TYPES: Record<NoticeType, { label: string; color: string; icon: string }> = {
  system: {
    label: '系统通知',
    color: 'red',
    icon: '<svg class="icon icon-sm text-secondary" xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"/><path d="M3 12a9 9 0 1 0 18 0a9 9 0 0 0 -18 0" /><path d="M12 9h.01" /><path d="M11 12h1v4h1" /></svg>',
  },
  mention: {
    label: '@提醒',
    color: 'blue',
    icon: '<svg class="icon icon-sm text-secondary" xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"/><path d="M12 12m-4 0a4 4 0 1 0 8 0a4 4 0 1 0 -8 0" /><path d="M16 12v1.5a2.5 2.5 0 0 0 5 0v-1.5a9 9 0 1 0 -5.5 8.28" /></svg>',
  },
  comment: {
    label: '评论',
    color: 'green',
    icon: '<svg class="icon icon-sm text-secondary" xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"/><path d="M8 9h8" /><path d="M8 13h6" /><path d="M18 4a3 3 0 0 1 3 3v8a3 3 0 0 1 -3 3h-5l-5 3v-3h-2a3 3 0 0 1 -3 -3v-8a3 3 0 0 1 3 -3h12z" /></svg>',
  },
  like: {
    label: '点赞',
    color: 'pink',
    icon: '<svg class="icon icon-sm text-secondary" xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" stroke-width="2" stroke="currentColor" fill="none" stroke-linecap="round" stroke-linejoin="round"><path stroke="none" d="M0 0h24v24H0z" fill="none"/><path d="M19.5 12.572l-7.5 7.428l-7.5 -7.428a5 5 0 1 1 7.5 -6.566a5 5 0 1 1 7.5 6.566z" /></svg>',
  },
};

/** 排序优先级：系统 > @提醒 > 评论 > 点赞 */
const TYPE_PRIORITY: NoticeType[] = ['system', 'mention', 'comment', 'like'];

@Component({
  selector: 'header[layout-header]',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink],
  host: {
    // 用 expand-lg 与侧边栏断点对齐：否则 768–991px 汉堡键被隐藏、侧边栏又是抽屉，导航无法打开
    class: 'navbar navbar-expand-lg sticky-top d-print-none',
    '[attr.aria-label]': '"顶部导航"',
  },
  template: `
    <div class="container-xl">
      <!-- 移动端：打开侧边栏抽屉 -->
      <button
        class="navbar-toggler d-lg-none"
        type="button"
        (click)="layout.toggleMobile()"
        [attr.aria-expanded]="layout.mobileOpen()"
        aria-controls="aside-menu"
        aria-label="打开导航"
      >
        <!-- Tabler 自带的 toggler 图标（1.5em，mask 绘制，与预览页一致）；
             换成内联 svg.icon 会被 .icon 规则压成 8px 宽 -->
        <span class="navbar-toggler-icon"></span>
      </button>

      <!-- 桌面端：折叠 / 展开侧边栏 -->
      <button
        class="btn btn-ghost-secondary btn-icon d-none d-lg-flex"
        type="button"
        (click)="layout.toggleCollapsed()"
        [attr.aria-expanded]="!layout.collapsed()"
        aria-controls="aside-menu"
        [attr.aria-label]="layout.collapsed() ? '展开侧边栏' : '折叠侧边栏'"
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
            <path d="M11 7l-5 5l-5 5" />
          </svg>
        }
      </button>

      <!-- 动作区：顺序参照 Tabler 示例（主题 → 通知 → 语言 → 用户），全部用 .nav-link 统一外观 -->
      <div class="navbar-nav flex-row order-md-last ms-auto align-items-center">
        <div class="nav-item d-none d-md-flex">
          <a routerLink="/dashboard" class="nav-link px-2" aria-label="返回工作台">
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

        <!-- 亮/暗切换：显示哪个按钮由 Tabler 的 .hide-theme-dark / .hide-theme-light
             依据 data-bs-theme 决定，组件无需维护当前主题状态 -->
        <div class="nav-item d-flex">
          <a
            href="javascript:void(0)"
            class="nav-link px-2 hide-theme-dark"
            (click)="settings.setTheme('dark')"
            title="切换到暗色模式"
            aria-label="切换到暗色模式"
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
            title="切换到亮色模式"
            aria-label="切换到亮色模式"
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

        <!-- 通知：红点依赖 .nav-link > .badge 的绝对定位规则，不能换成 btn -->
        <div class="nav-item dropdown">
          <a
            #notifyToggle
            href="javascript:void(0)"
            class="nav-link px-2"
            data-bs-toggle="dropdown"
            data-bs-auto-close="outside"
            aria-label="通知"
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
                <h2 class="card-title">通知</h2>
                <span class="ms-2 small text-secondary">{{ unreadCount() }} 条未读</span>
                <button
                  type="button"
                  class="btn-close ms-auto"
                  (click)="closeNotifications()"
                  aria-label="关闭"
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
                            <span class="visually-hidden">{{ item.unread ? '未读' : '已读' }}</span>
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
                            [attr.aria-label]="item.starred ? '取消收藏' : '收藏'"
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
                        <div>暂无通知</div>
                      </div>
                    </div>
                  }
                }
              </div>

              <div class="card-body">
                <div class="row g-2">
                  <div class="col">
                    <button class="btn w-100" type="button" (click)="archiveAll()">归档全部</button>
                  </div>
                  <div class="col">
                    <button
                      class="btn w-100"
                      type="button"
                      [disabled]="unreadCount() === 0"
                      (click)="markAllRead()"
                    >
                      全部标记已读
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 语言：shortcut: 只记录偏好与 <html lang>，项目尚未接入 i18n 资源，接入后由它驱动文案 -->
        <div class="nav-item dropdown d-none d-md-flex">
          <a
            href="javascript:void(0)"
            class="nav-link px-2 d-flex align-items-center"
            data-bs-toggle="dropdown"
            aria-label="切换语言"
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
            <button class="dropdown-item" type="button" (click)="setLang('zh-CN')">简体中文</button>
            <button class="dropdown-item" type="button" (click)="setLang('en')">English</button>
          </div>
        </div>

        <div class="nav-item dropdown">
          <a
            href="javascript:void(0)"
            class="nav-link d-flex lh-1 p-0 px-2"
            data-bs-toggle="dropdown"
            aria-label="打开用户菜单"
          >
            <span class="avatar avatar-sm bg-primary-lt">{{ user.initial() }}</span>
            <!-- 只展示姓名与昵称，角色/权限不在界面上出现 -->
            <div class="d-none d-xl-block ps-2 lh-sm">
              <div class="fw-medium">{{ user.displayName() }}</div>
              @if (showNickname()) {
                <div class="small text-secondary">{{ user.nickname() }}</div>
              }
            </div>
          </a>
          <div class="dropdown-menu dropdown-menu-end dropdown-menu-arrow">
            <!-- xl 以下顶栏只剩头像，名字挪进菜单里，否则小屏看不出当前是谁 -->
            <div class="d-xl-none">
              <div class="dropdown-item-text d-flex align-items-center">
                <span class="avatar avatar-sm bg-primary-lt">{{ user.initial() }}</span>
                <div class="ps-2 lh-sm">
                  <div class="fw-medium">{{ user.displayName() }}</div>
                  @if (showNickname()) {
                    <div class="small text-secondary">{{ user.nickname() }}</div>
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
              工作台
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
              退出登录
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
  protected readonly userSer = inject(UserService);
  protected readonly settings = inject(SettingsService);
  protected readonly layout = inject(LayoutService);

  /** 昵称与主显示名相同时只显示一行，避免「张三 / 张三」 */
  protected readonly showNickname = computed(() => {
    const nickname = this.userSer.nickname();
    return nickname !== '' && nickname !== this.userSer.displayName();
  });

  // 图标是硬编码的静态 SVG；Angular 的 innerHTML 清洗会移除 <svg>，故声明为可信内容
  private readonly sanitizer = inject(DomSanitizer);
  private readonly typeIcons = new Map<NoticeType, SafeHtml>(
    (Object.keys(NOTICE_TYPES) as NoticeType[]).map((type) => [
      type,
      this.sanitizer.bypassSecurityTrustHtml(NOTICE_TYPES[type].icon),
    ]),
  );

  private readonly notifyToggle = viewChild.required<ElementRef<HTMLElement>>('notifyToggle');

  /** 通知示例数据：暂无后端接口，先用于验证结构与交互 */
  protected readonly notices = signal<ReadonlyArray<Notice>>([
    {
      id: 1,
      type: 'system',
      title: '版本 1.4.0 已发布',
      text: '修复租户切换后权限未刷新的问题 (#29604)',
      time: '10 分钟前',
      at: Date.now() - 10 * 60_000,
      unread: true,
      starred: false,
    },
    {
      id: 2,
      type: 'mention',
      title: '张伟 在「用户管理」中 @ 了你',
      text: '麻烦确认下这个租户的分组权限是否正确 (#29734)',
      time: '1 小时前',
      at: Date.now() - 60 * 60_000,
      unread: true,
      starred: true,
    },
    {
      id: 3,
      type: 'comment',
      title: '李娜 评论了你的角色配置',
      text: '建议把「只读」角色拆成两个更细的权限 (#29736)',
      time: '3 小时前',
      at: Date.now() - 3 * 60 * 60_000,
      unread: false,
      starred: false,
    },
    {
      id: 4,
      type: 'like',
      title: '王强 赞了你的月度报表',
      text: '「月度活跃用户」报表收到 5 个赞 (#29730)',
      time: '昨天 18:02',
      at: Date.now() - 26 * 60 * 60_000,
      unread: true,
      starred: false,
    },
  ]);

  /** 排序：未读优先 → 类型优先级（系统 > @提醒 > 评论 > 点赞）→ 时间倒序 */
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
  protected readonly langLabel = computed(() => (this.lang() === 'en' ? 'EN' : '中文'));

  colorOf(type: NoticeType): string {
    return NOTICE_TYPES[type].color;
  }

  iconOf(type: NoticeType): SafeHtml {
    return this.typeIcons.get(type)!;
  }

  /** 首次打开时加载；shortcut: 暂无通知接口，用 400ms 模拟一次拉取，接入后替换成真实请求 */
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

  /** 关闭面板用 Tabler 的 Dropdown 实例（Bootstrap 5 的 dropdown 不支持 data-bs-dismiss） */
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
    this.tokenSer.logout();
    void this.router.navigate([this.tokenSer.loginUrl]);
  }
}

import {Component, ElementRef, inject, OnInit, output, signal} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {ThemeService} from '@app/core/services/theme.service';
import {Authentication} from '@app/core/types';
import {LoginService} from '@app/pages';
import {SharedModule} from '@app/shared/shared.module';

@Component({
  selector: 'layout-navbar,[layoutNavbar]',
  imports: [SharedModule],
  template: ` <div class="navbar d-print-none">
    <div class="container-fluid">
      <button
        class="navbar-toggler"
        type="button"
        (click)="onCollapsed(!isCollapsed())"
        aria-controls="navbar-menu"
        aria-expanded="true"
        aria-label="Toggle navigation">
        <span [nzType]="isCollapsed() ? 'menu-unfold' : 'menu-fold'" class="trigger" nz-icon></span>
      </button>
      <div class="navbar-brand navbar-brand-autodark d-none-navbar-horizontal pe-0 pe-md-3"></div>
      <div class="navbar-nav flex-row order-md-last">
        <div class="nav-item d-none d-md-flex me-3">
          <div class="btn-list">
            <a href="https://github.com/vnobo/plate" class="btn" target="_blank" rel="noreferrer">
              <!-- Download SVG icon from http://tabler-icons.io/i/brand-github -->
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
                class="icon">
                <path stroke="none" d="M0 0h24v24H0z" fill="none"></path>
                <path
                  d="M9 19c-4.3 1.4 -4.3 -2.5 -6 -3m12 5v-3.5c0 -1 .1 -1.4 -.5 -2c2.8 -.3 5.5 -1.4 5.5 -6a4.6 4.6 0 0 0 -1.3 -3.2a4.2 4.2 0 0 0 -.1 -3.2s-1.1 -.3 -3.5 1.3a12.3 12.3 0 0 0 -6.2 0c-2.4 -1.6 -3.5 -1.3 -3.5 -1.3a4.2 4.2 0 0 0 -.1 3.2a4.6 4.6 0 0 0 -1.3 3.2c0 4.6 2.7 5.7 5.5 6c-.6 .6 -.6 1.2 -.5 2v3.5"></path>
              </svg>
              Source code
            </a>
            <a href="#" class="btn" target="_blank" rel="noreferrer">
              <!-- Download SVG icon from http://tabler-icons.io/i/heart -->
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
                class="icon text-pink">
                <path stroke="none" d="M0 0h24v24H0z" fill="none"></path>
                <path d="M19.5 12.572l-7.5 7.428l-7.5 -7.428a5 5 0 1 1 7.5 -6.566a5 5 0 1 1 7.5 6.572"></path>
              </svg>
              Sponsor
            </a>
          </div>
        </div>
        <div class="d-none d-md-flex">
          <a
            class="nav-link px-0 hide-theme-dark"
            data-bs-toggle="tooltip"
            data-bs-placement="bottom"
            aria-label="暗黑主题"
            data-bs-original-title="暗黑主题"
            (click)="onToggleTheme()">
            <!-- Download SVG icon from http://tabler-icons.io/i/moon -->
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
              class="icon">
              <path stroke="none" d="M0 0h24v24H0z" fill="none"></path>
              <path d="M12 3c.132 0 .263 0 .393 0a7.5 7.5 0 0 0 7.92 12.446a9 9 0 1 1 -8.313 -12.454z"></path>
            </svg>
          </a>
          <a
            (click)="onToggleTheme()"
            class="nav-link px-0 hide-theme-light"
            data-bs-toggle="tooltip"
            data-bs-placement="bottom"
            aria-label="浅色模式"
            data-bs-original-title="浅色模式">
            <!-- Download SVG icon from http://tabler-icons.io/i/sun -->
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
              class="icon">
              <path stroke="none" d="M0 0h24v24H0z" fill="none"></path>
              <path d="M12 12m-4 0a4 4 0 1 0 8 0a4 4 0 1 0 -8 0"></path>
              <path
                d="M3 12h1m8 -9v1m8 8h1m-9 8v1m-6.4 -15.4l.7 .7m12.1 -.7l-.7 .7m0 11.4l.7 .7m-12.1 -.7l-.7 .7"></path>
            </svg>
          </a>
          <div class="nav-item dropdown d-none d-md-flex me-3">
            <a href="#" class="nav-link px-0" data-bs-toggle="dropdown" tabindex="-1" aria-label="Show notifications">
              <!-- Download SVG icon from http://tabler-icons.io/i/bell -->
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
                class="icon">
                <path stroke="none" d="M0 0h24v24H0z" fill="none"></path>
                <path d="M10 5a2 2 0 1 1 4 0a7 7 0 0 1 4 6v3a4 4 0 0 0 2 3h-16a4 4 0 0 0 2 -3v-3a7 7 0 0 1 4 -6"></path>
                <path d="M9 17v1a3 3 0 0 0 6 0v-1"></path>
              </svg>
              <span class="badge bg-red"></span>
            </a>
            <div class="dropdown-menu dropdown-menu-arrow dropdown-menu-end dropdown-menu-card">
              <div class="card">
                <div class="card-header">
                  <h3 class="card-title">Last updates</h3>
                </div>
                <div class="list-group list-group-flush list-group-hoverable">
                  <div class="list-group-item">
                    <div class="row align-items-center">
                      <div class="col-auto"><span class="status-dot status-dot-animated bg-red d-block"></span></div>
                      <div class="col text-truncate">
                        <a href="#" class="text-body d-block">Example 1</a>
                        <div class="d-block text-secondary text-truncate mt-n1">
                          Change deprecated html tags to text decoration classes (#29604)
                        </div>
                      </div>
                      <div class="col-auto">
                        <a href="#" class="list-group-item-actions">
                          <!-- Download SVG icon from http://tabler-icons.io/i/star -->
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
                            class="icon text-muted">
                            <path stroke="none" d="M0 0h24v24H0z" fill="none"></path>
                            <path
                              d="M12 17.75l-6.172 3.245l1.179 -6.873l-5 -4.867l6.9 -1l3.086 -6.253l3.086 6.253l6.9 1l-5 4.867l1.179 6.873z"></path>
                          </svg>
                        </a>
                      </div>
                    </div>
                  </div>
                  <div class="list-group-item">
                    <div class="row align-items-center">
                      <div class="col-auto"><span class="status-dot d-block"></span></div>
                      <div class="col text-truncate">
                        <a href="#" class="text-body d-block">Example 2</a>
                        <div class="d-block text-secondary text-truncate mt-n1">
                          justify-content:between ⇒ justify-content:space-between (#29734)
                        </div>
                      </div>
                      <div class="col-auto">
                        <a href="#" class="list-group-item-actions show">
                          <!-- Download SVG icon from http://tabler-icons.io/i/star -->
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
                            class="icon text-yellow">
                            <path stroke="none" d="M0 0h24v24H0z" fill="none"></path>
                            <path
                              d="M12 17.75l-6.172 3.245l1.179 -6.873l-5 -4.867l6.9 -1l3.086 -6.253l3.086 6.253l6.9 1l-5 4.867l1.179 6.873z"></path>
                          </svg>
                        </a>
                      </div>
                    </div>
                  </div>
                  <div class="list-group-item">
                    <div class="row align-items-center">
                      <div class="col-auto"><span class="status-dot d-block"></span></div>
                      <div class="col text-truncate">
                        <a href="#" class="text-body d-block">Example 3</a>
                        <div class="d-block text-secondary text-truncate mt-n1">
                          Update change-version.js (#29736)
                        </div>
                      </div>
                      <div class="col-auto">
                        <a href="#" class="list-group-item-actions">
                          <!-- Download SVG icon from http://tabler-icons.io/i/star -->
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
                            class="icon text-muted">
                            <path stroke="none" d="M0 0h24v24H0z" fill="none"></path>
                            <path
                              d="M12 17.75l-6.172 3.245l1.179 -6.873l-5 -4.867l6.9 -1l3.086 -6.253l3.086 6.253l6.9 1l-5 4.867l1.179 6.873z"></path>
                          </svg>
                        </a>
                      </div>
                    </div>
                  </div>
                  <div class="list-group-item">
                    <div class="row align-items-center">
                      <div class="col-auto"><span class="status-dot status-dot-animated bg-green d-block"></span> </div>
                      <div class="col text-truncate">
                        <a href="#" class="text-body d-block">Example 4</a>
                        <div class="d-block text-secondary text-truncate mt-n1">
                          Regenerate package-lock.json (#29730)
                        </div>
                      </div>
                      <div class="col-auto">
                        <a href="#" class="list-group-item-actions">
                          <!-- Download SVG icon from http://tabler-icons.io/i/star -->
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
                            class="icon text-muted">
                            <path stroke="none" d="M0 0h24v24H0z" fill="none"></path>
                            <path
                              d="M12 17.75l-6.172 3.245l1.179 -6.873l-5 -4.867l6.9 -1l3.086 -6.253l3.086 6.253l6.9 1l-5 4.867l1.179 6.873z"></path>
                          </svg>
                        </a>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div class="nav-item dropdown">
          <a
            href="#"
            class="nav-link d-flex lh-1 text-reset p-0"
            data-bs-toggle="dropdown"
            aria-label="Open user menu"
            aria-expanded="false">
            <span class="avatar avatar-sm" style="background-image: url(/assets/img/avater.png)"></span>
            <div class="d-none d-xl-block ps-2">
              <span>{{ authenticationToken()?.details?.nickname }}</span>
              <div class="mt-1 small text-secondary">UI Designer</div>
            </div>
          </a>
          <div class="dropdown-menu dropdown-menu-end dropdown-menu-arrow">
            <a href="#" class="dropdown-item">Status</a>
            <a href="#" class="dropdown-item">Profile</a>
            <a href="#" class="dropdown-item">Feedback</a>
            <div class="dropdown-divider"></div>
            <a href="#" class="dropdown-item">Settings</a>
            <a class="dropdown-item" (click)="loginOut()">注销用户</a>
          </div>
        </div>
      </div>
    </div>
  </div>`,
  styles: [
    `
      :host {
        min-height: 100%;
        min-width: 100%;
      }
    `,
    `
      .nz-page-header {
        position: relative;
        padding: 0;
        box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
        height: 100%;
      }
    `,
    `
      .header-trigger {
        height: 64px;
        padding: 20px 24px;
        font-size: 20px;
        cursor: pointer;
        transition: all 0.3s, padding 0s;
      }
    `,
  ],
})
export class LayoutNavbarComponent implements OnInit {
  private readonly _loginSer = inject(LoginService);
  private readonly _themeSer = inject(ThemeService);

  constructor(private _router: Router, private _route: ActivatedRoute, private _el: ElementRef) {}

  outputCollapsed = output<boolean>();
  authenticationToken = signal<Authentication | null>({} as Authentication);
  isCollapsed = signal(false);

  ngOnInit(): void {
    this.authenticationToken.set(this._loginSer._auth.authenticationToken());
    this.initializeTooltips();
  }

  onToggleTheme() {
    this._themeSer
      .toggleTheme()
      .then(res => console.log('切换主题成功!', res))
      .catch(err => console.log('切换主题失败!', err));
  }

  onCollapsed(isCollapsed: boolean) {
    this.isCollapsed.set(isCollapsed);
    this.outputCollapsed.emit(isCollapsed);
  }

  loginOut() {
    this._loginSer.logout();
    this._router.navigate([this._loginSer._auth.loginUrl], { relativeTo: this._route }).then();
  }

  private initializeTooltips(): void {
    const tooltipTriggerList = Array.from(this._el.nativeElement.querySelectorAll('[data-bs-toggle="tooltip"]'));
    tooltipTriggerList.forEach((tooltipTriggerEl: any) => {
      const options = {
        delay: { show: 50, hide: 50 },
        html: tooltipTriggerEl.getAttribute('data-bs-html') === 'true',
        placement: tooltipTriggerEl.getAttribute('data-bs-placement') ?? 'auto',
      };
      return new bootstrap.Tooltip(tooltipTriggerEl, options);
    });
  }
}

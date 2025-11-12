import { Component, inject, signal, afterNextRender, ChangeDetectionStrategy } from '@angular/core';
import { TokenService } from '@app/core';
import { UserDetails } from '@plate/types';

@Component({
  selector: 'app-layout-header',
  template: `
    <header class="navbar navbar-expand-md d-print-none">
      <div class="container-fluid">
        <!-- BEGIN NAVBAR TOGGLER -->
        <button
          class="navbar-toggler"
          type="button"
          data-bs-toggle="collapse"
          data-bs-target="#navbar-menu"
          aria-controls="navbar-menu"
          aria-expanded="true"
          aria-label="Toggle navigation"
        >
          <span class="navbar-toggler-icon"></span>
        </button>
        <!-- END NAVBAR TOGGLER -->
        <!-- BEGIN NAVBAR LOGO -->
        <div class="navbar-brand navbar-brand-autodark d-none-navbar-horizontal pe-0 pe-md-3">
          <a href="." aria-label="Tabler"
            ><svg
              xmlns="http://www.w3.org/2000/svg"
              viewBox="0 0 68 68"
              width="32"
              height="32"
              aria-label="Tabler"
              class="navbar-brand-image me-3"
            >
              <path
                d="M64.6 16.2C63 9.9 58.1 5 51.8 3.4 40 1.5 28 1.5 16.2 3.4 9.9 5 5 9.9 3.4 16.2 1.5 28 1.5 40 3.4 51.8 5 58.1 9.9 63 16.2 64.6c11.8 1.9 23.8 1.9 35.6 0C58.1 63 63 58.1 64.6 51.8c1.9-11.8 1.9-23.8 0-35.6zM33.3 36.3c-2.8 4.4-6.6 8.2-11.1 11-1.5.9-3.3.9-4.8.1s-2.4-2.3-2.5-4c0-1.7.9-3.3 2.4-4.1 2.3-1.4 4.4-3.2 6.1-5.3-1.8-2.1-3.8-3.8-6.1-5.3-2.3-1.3-3-4.2-1.7-6.4s4.3-2.9 6.5-1.6c4.5 2.8 8.2 6.5 11.1 10.9 1 1.4 1 3.3.1 4.7zM49.2 46H37.8c-2.1 0-3.8-1-3.8-3s1.7-3 3.8-3h11.4c2.1 0 3.8 1 3.8 3s-1.7 3-3.8 3z"
                fill="#066fd1"
                style="fill: var(--tblr-primary, #066fd1)"
              ></path>
            </svg>
            Dashboard
          </a>
        </div>
        <!-- END NAVBAR LOGO -->
        <div class="navbar-nav flex-row order-md-last">
          <div class="nav-item d-none d-md-flex me-3">
            <div class="btn-list">
              <a href="https://github.com/tabler/tabler" class="btn btn-5" target="_blank">
                <svg
                  class="icon"
                  width="24"
                  height="24"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                >
                  <path
                    d="M9 19c-5 1.5-5-2.5-7-3m14 6v-3.87a3.37 3.37 0 0 0-.94-2.61c3.14-.35 6.44-1.54 6.44-7A5.44 5.44 0 0 0 20 4.77 5.07 5.07 0 0 0 19.91 1S18.73.65 16 2.48a13.38 13.38 0 0 0-7 0C6.27.65 5.09 1 5.09 1A5.07 5.07 0 0 0 5 4.77a5.44 5.44 0 0 0-1.5 3.78c0 5.42 3.3 6.61 6.44 7A3.37 3.37 0 0 0 9 18.13V22"
                  ></path>
                </svg>
                Source code
              </a>
              <a href="https://github.com/sponsors/codecalm" class="btn btn-5" target="_blank">
                <svg
                  class="icon"
                  width="24"
                  height="24"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                >
                  <path
                    d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"
                  ></path>
                </svg>
                Sponsor
              </a>
            </div>
          </div>
          <div class="nav-item dropdown">
            <a
              href="#"
              class="nav-link d-flex lh-1 text-reset p-0"
              data-bs-toggle="dropdown"
              aria-label="Open user menu"
            >
              <span class="avatar avatar-sm" [style]="userAvatar()"></span>
              <div class="d-none d-xl-block ps-2">
                <div>{{ userDetails().name }}</div>
                <div class="mt-1 small text-secondary">{{ userDetails().nickname }}</div>
              </div>
            </a>
            <div class="dropdown-menu dropdown-menu-end dropdown-menu-arrow">
              <a href="#" class="dropdown-item">Status</a>
              <a href="./profile.html" class="dropdown-item">Profile</a>
              <a href="#" class="dropdown-item">Feedback</a>
              <div class="dropdown-divider"></div>
              <a href="./settings.html" class="dropdown-item">Settings</a>
              <a href="./sign-in.html" class="dropdown-item">Logout</a>
            </div>
          </div>
        </div>
      </div>
    </header>
  `,
  styles: [
    `
      :host {
        display: block;
      }
      .avatar {
        background-image: url('/assets/img/avater.png');
      }
    `,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LayoutHeader {
  private readonly _tokenSer = inject(TokenService);

  userDetails = signal({} as UserDetails);
  userAvatar = signal({
    backgroundImage: "url('assets/img/avater.png')",
  });

  constructor() {
    afterNextRender(() => {
      this._tokenSer.isLoggedIn$.subscribe((isLoggedIn) => {
        if (isLoggedIn) {
          const authentication = this._tokenSer.authenticationToken();
          if (authentication) {
            this.userDetails.set(authentication.details);
            if (authentication.details.avatar) {
              this.userAvatar.set({
                backgroundImage: 'url(' + authentication.details.avatar + ')',
              });
            }
          }
        }
      });
    });
  }
}

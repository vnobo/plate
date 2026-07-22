import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';

@Component({
  selector: 'app-layout-aside',
  imports: [RouterLink, RouterLinkActive],
  template: `
    <header class="navbar-expand-md">
      <div class="collapse navbar-collapse" id="navbar-menu">
        <div class="navbar">
          <div class="container-fluid">
            <div class="row flex-column flex-md-row flex-fill align-items-center">
              <div class="col">
                <ul class="navbar-nav">
                  <li class="nav-item">
                    <a class="nav-link" routerLink="./tenant" routerLinkActive="active">
                      <span class="nav-link-icon d-md-none d-lg-inline-block">
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
                          class="icon icon-1"
                        >
                          <path d="M5 12l-2 0l9 -9l9 9l-2 0"></path>
                          <path d="M5 12v7a2 2 0 0 0 2 2h10a2 2 0 0 0 2 -2v-7"></path>
                          <path d="M9 21v-6a2 2 0 0 1 2 -2h2a2 2 0 0 1 2 2v6"></path>
                        </svg>
                      </span>
                      <span class="nav-link-title">租户管理</span>
                    </a>
                  </li>
                  <li class="nav-item">
                    <a class="nav-link" routerLink="./role" routerLinkActive="active">
                      <span class="nav-link-icon d-md-none d-lg-inline-block">
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
                          class="icon icon-1"
                        >
                          <path
                            d="M12 3a12 12 0 0 0 8.5 3a12 12 0 0 1 -8.5 15a12 12 0 0 1 -8.5 -15a12 12 0 0 0 8.5 -3"
                          ></path>
                        </svg>
                      </span>
                      <span class="nav-link-title">角色管理</span>
                    </a>
                  </li>
                </ul>
              </div>
              <div class="col col-md-auto"></div>
            </div>
          </div>
        </div>
      </div>
    </header>
  `,
  styles: `
    :host {
      display: contents;
    }
  `,
})
export class LayoutAside {}

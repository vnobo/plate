import { afterNextRender, Component, inject, OnInit, signal } from '@angular/core';
import { RouterModule } from '@angular/router';
import { TokenService } from '@app/core';
import { UserDetails } from '@plate/types';

@Component({
  selector: 'app-layout-base',
  imports: [RouterModule],
  template: `<div class="page">
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
          aria-label="Toggle navigation">
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
              class="navbar-brand-image me-3">
              <path
                d="M64.6 16.2C63 9.9 58.1 5 51.8 3.4 40 1.5 28 1.5 16.2 3.4 9.9 5 5 9.9 3.4 16.2 1.5 28 1.5 40 3.4 51.8 5 58.1 9.9 63 16.2 64.6c11.8 1.9 23.8 1.9 35.6 0C58.1 63 63 58.1 64.6 51.8c1.9-11.8 1.9-23.8 0-35.6zM33.3 36.3c-2.8 4.4-6.6 8.2-11.1 11-1.5.9-3.3.9-4.8.1s-2.4-2.3-2.5-4c0-1.7.9-3.3 2.4-4.1 2.3-1.4 4.4-3.2 6.1-5.3-1.8-2.1-3.8-3.8-6.1-5.3-2.3-1.3-3-4.2-1.7-6.4s4.3-2.9 6.5-1.6c4.5 2.8 8.2 6.5 11.1 10.9 1 1.4 1 3.3.1 4.7zM49.2 46H37.8c-2.1 0-3.8-1-3.8-3s1.7-3 3.8-3h11.4c2.1 0 3.8 1 3.8 3s-1.7 3-3.8 3z"
                fill="#066fd1"
                style="fill: var(--tblr-primary, #066fd1)"></path>
            </svg>
            Dashboard
          </a>
        </div>
        <!-- END NAVBAR LOGO -->
        <div class="navbar-nav flex-row order-md-last">
          <div class="nav-item d-none d-md-flex me-3">
            <div class="btn-list">
              <a
                href="https://github.com/tabler/tabler"
                class="btn btn-5"
                target="_blank"
                rel="noreferrer">
                <!-- Download SVG icon from http://tabler.io/icons/icon/brand-github -->
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
                  class="icon icon-2">
                  <path
                    d="M9 19c-4.3 1.4 -4.3 -2.5 -6 -3m12 5v-3.5c0 -1 .1 -1.4 -.5 -2c2.8 -.3 5.5 -1.4 5.5 -6a4.6 4.6 0 0 0 -1.3 -3.2a4.2 4.2 0 0 0 -.1 -3.2s-1.1 -.3 -3.5 1.3a12.3 12.3 0 0 0 -6.2 0c-2.4 -1.6 -3.5 -1.3 -3.5 -1.3a4.2 4.2 0 0 0 -.1 3.2a4.6 4.6 0 0 0 -1.3 3.2c0 4.6 2.7 5.7 5.5 6c-.6 .6 -.6 1.2 -.5 2v3.5"></path>
                </svg>
                Source code
              </a>
              <a
                href="https://github.com/sponsors/codecalm"
                class="btn btn-6"
                target="_blank"
                rel="noreferrer">
                <!-- Download SVG icon from http://tabler.io/icons/icon/heart -->
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
                  class="icon text-pink icon-2">
                  <path
                    d="M19.5 12.572l-7.5 7.428l-7.5 -7.428a5 5 0 1 1 7.5 -6.566a5 5 0 1 1 7.5 6.572"></path>
                </svg>
                Sponsor
              </a>
            </div>
          </div>
          <div class="d-none d-md-flex">
            <div class="nav-item">
              <a
                href="?theme=dark"
                class="nav-link px-0 hide-theme-dark"
                data-bs-toggle="tooltip"
                data-bs-placement="bottom"
                aria-label="Enable dark mode"
                data-bs-original-title="Enable dark mode">
                <!-- Download SVG icon from http://tabler.io/icons/icon/moon -->
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
                  class="icon icon-1">
                  <path
                    d="M12 3c.132 0 .263 0 .393 0a7.5 7.5 0 0 0 7.92 12.446a9 9 0 1 1 -8.313 -12.454z"></path>
                </svg>
              </a>
              <a
                href="?theme=light"
                class="nav-link px-0 hide-theme-light"
                data-bs-toggle="tooltip"
                data-bs-placement="bottom"
                aria-label="Enable light mode"
                data-bs-original-title="Enable light mode">
                <!-- Download SVG icon from http://tabler.io/icons/icon/sun -->
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
                  class="icon icon-1">
                  <path d="M12 12m-4 0a4 4 0 1 0 8 0a4 4 0 1 0 -8 0"></path>
                  <path
                    d="M3 12h1m8 -9v1m8 8h1m-9 8v1m-6.4 -15.4l.7 .7m12.1 -.7l-.7 .7m0 11.4l.7 .7m-12.1 -.7l-.7 .7"></path>
                </svg>
              </a>
            </div>
            <div class="nav-item dropdown d-none d-md-flex">
              <a
                href="#"
                class="nav-link px-0"
                data-bs-toggle="dropdown"
                tabindex="-1"
                aria-label="Show notifications"
                data-bs-auto-close="outside"
                aria-expanded="false">
                <!-- Download SVG icon from http://tabler.io/icons/icon/bell -->
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
                  class="icon icon-1">
                  <path
                    d="M10 5a2 2 0 1 1 4 0a7 7 0 0 1 4 6v3a4 4 0 0 0 2 3h-16a4 4 0 0 0 2 -3v-3a7 7 0 0 1 4 -6"></path>
                  <path d="M9 17v1a3 3 0 0 0 6 0v-1"></path>
                </svg>
                <span class="badge bg-red"></span>
              </a>
              <div class="dropdown-menu dropdown-menu-arrow dropdown-menu-end dropdown-menu-card">
                <div class="card">
                  <div class="card-header d-flex">
                    <h3 class="card-title">Notifications</h3>
                    <div class="btn-close ms-auto" data-bs-dismiss="dropdown"></div>
                  </div>
                  <div class="list-group list-group-flush list-group-hoverable">
                    <div class="list-group-item">
                      <div class="row align-items-center">
                        <div class="col-auto"
                          ><span class="status-dot status-dot-animated bg-red d-block"></span
                        ></div>
                        <div class="col text-truncate">
                          <a href="#" class="text-body d-block">Example 1</a>
                          <div class="d-block text-secondary text-truncate mt-n1">
                            Change deprecated html tags to text decoration classes (#29604)
                          </div>
                        </div>
                        <div class="col-auto">
                          <a href="#" class="list-group-item-actions">
                            <!-- Download SVG icon from http://tabler.io/icons/icon/star -->
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
                              class="icon text-muted icon-2">
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
                            <!-- Download SVG icon from http://tabler.io/icons/icon/star -->
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
                              class="icon text-yellow icon-2">
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
                          <div class="d-block text-secondary text-truncate mt-n1"
                            >Update change-version.js (#29736)</div
                          >
                        </div>
                        <div class="col-auto">
                          <a href="#" class="list-group-item-actions">
                            <!-- Download SVG icon from http://tabler.io/icons/icon/star -->
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
                              class="icon text-muted icon-2">
                              <path
                                d="M12 17.75l-6.172 3.245l1.179 -6.873l-5 -4.867l6.9 -1l3.086 -6.253l3.086 6.253l6.9 1l-5 4.867l1.179 6.873z"></path>
                            </svg>
                          </a>
                        </div>
                      </div>
                    </div>
                    <div class="list-group-item">
                      <div class="row align-items-center">
                        <div class="col-auto"
                          ><span class="status-dot status-dot-animated bg-green d-block"></span
                        ></div>
                        <div class="col text-truncate">
                          <a href="#" class="text-body d-block">Example 4</a>
                          <div class="d-block text-secondary text-truncate mt-n1"
                            >Regenerate package-lock.json (#29730)</div
                          >
                        </div>
                        <div class="col-auto">
                          <a href="#" class="list-group-item-actions">
                            <!-- Download SVG icon from http://tabler.io/icons/icon/star -->
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
                              class="icon text-muted icon-2">
                              <path
                                d="M12 17.75l-6.172 3.245l1.179 -6.873l-5 -4.867l6.9 -1l3.086 -6.253l3.086 6.253l6.9 1l-5 4.867l1.179 6.873z"></path>
                            </svg>
                          </a>
                        </div>
                      </div>
                    </div>
                  </div>
                  <div class="card-body">
                    <div class="row">
                      <div class="col">
                        <a href="#" class="btn btn-2 w-100"> Archive all </a>
                      </div>
                      <div class="col">
                        <a href="#" class="btn btn-2 w-100"> Mark all as read </a>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            <div class="nav-item dropdown d-none d-md-flex me-3">
              <a
                href="#"
                class="nav-link px-0"
                data-bs-toggle="dropdown"
                tabindex="-1"
                aria-label="Show app menu"
                data-bs-auto-close="outside"
                aria-expanded="false">
                <!-- Download SVG icon from http://tabler.io/icons/icon/apps -->
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
                  class="icon icon-1">
                  <path
                    d="M4 4m0 1a1 1 0 0 1 1 -1h4a1 1 0 0 1 1 1v4a1 1 0 0 1 -1 1h-4a1 1 0 0 1 -1 -1z"></path>
                  <path
                    d="M4 14m0 1a1 1 0 0 1 1 -1h4a1 1 0 0 1 1 1v4a1 1 0 0 1 -1 1h-4a1 1 0 0 1 -1 -1z"></path>
                  <path
                    d="M14 14m0 1a1 1 0 0 1 1 -1h4a1 1 0 0 1 1 1v4a1 1 0 0 1 -1 1h-4a1 1 0 0 1 -1 -1z"></path>
                  <path d="M14 7l6 0"></path>
                  <path d="M17 4l0 6"></path>
                </svg>
              </a>
              <div class="dropdown-menu dropdown-menu-arrow dropdown-menu-end dropdown-menu-card">
                <div class="card">
                  <div class="card-header">
                    <div class="card-title">My Apps</div>
                    <div class="card-actions btn-actions">
                      <a href="#" class="btn-action">
                        <!-- Download SVG icon from http://tabler.io/icons/icon/settings -->
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
                          class="icon icon-1">
                          <path
                            d="M10.325 4.317c.426 -1.756 2.924 -1.756 3.35 0a1.724 1.724 0 0 0 2.573 1.066c1.543 -.94 3.31 .826 2.37 2.37a1.724 1.724 0 0 0 1.065 2.572c1.756 .426 1.756 2.924 0 3.35a1.724 1.724 0 0 0 -1.066 2.573c.94 1.543 -.826 3.31 -2.37 2.37a1.724 1.724 0 0 0 -2.572 1.065c-.426 1.756 -2.924 1.756 -3.35 0a1.724 1.724 0 0 0 -2.573 -1.066c-1.543 .94 -3.31 -.826 -2.37 -2.37a1.724 1.724 0 0 0 -1.065 -2.572c-1.756 -.426 -1.756 -2.924 0 -3.35a1.724 1.724 0 0 0 1.066 -2.573c-.94 -1.543 .826 -3.31 2.37 -2.37c1 .608 2.296 .07 2.572 -1.065z"></path>
                          <path d="M9 12a3 3 0 1 0 6 0a3 3 0 0 0 -6 0"></path>
                        </svg>
                      </a>
                    </div>
                  </div>
                  <div class="card-body scroll-y p-2" style="max-height: 50vh">
                    <div class="row g-0">
                      <div class="col-4">
                        <a
                          href="#"
                          class="d-flex flex-column flex-center text-center text-secondary py-2 px-2 link-hoverable">
                          <img
                            src="./static/brands/amazon.svg"
                            class="w-6 h-6 mx-auto mb-2"
                            width="24"
                            height="24"
                            alt="" />
                          <span class="h5">Amazon</span>
                        </a>
                      </div>
                      <div class="col-4">
                        <a
                          href="#"
                          class="d-flex flex-column flex-center text-center text-secondary py-2 px-2 link-hoverable">
                          <img
                            src="./static/brands/android.svg"
                            class="w-6 h-6 mx-auto mb-2"
                            width="24"
                            height="24"
                            alt="" />
                          <span class="h5">Android</span>
                        </a>
                      </div>
                      <div class="col-4">
                        <a
                          href="#"
                          class="d-flex flex-column flex-center text-center text-secondary py-2 px-2 link-hoverable">
                          <img
                            src="./static/brands/app-store.svg"
                            class="w-6 h-6 mx-auto mb-2"
                            width="24"
                            height="24"
                            alt="" />
                          <span class="h5">Apple App Store</span>
                        </a>
                      </div>
                      <div class="col-4">
                        <a
                          href="#"
                          class="d-flex flex-column flex-center text-center text-secondary py-2 px-2 link-hoverable">
                          <img
                            src="./static/brands/apple-podcast.svg"
                            class="w-6 h-6 mx-auto mb-2"
                            width="24"
                            height="24"
                            alt="" />
                          <span class="h5">Apple Podcast</span>
                        </a>
                      </div>
                      <div class="col-4">
                        <a
                          href="#"
                          class="d-flex flex-column flex-center text-center text-secondary py-2 px-2 link-hoverable">
                          <img
                            src="./static/brands/apple.svg"
                            class="w-6 h-6 mx-auto mb-2"
                            width="24"
                            height="24"
                            alt="" />
                          <span class="h5">Apple</span>
                        </a>
                      </div>
                      <div class="col-4">
                        <a
                          href="#"
                          class="d-flex flex-column flex-center text-center text-secondary py-2 px-2 link-hoverable">
                          <img
                            src="./static/brands/behance.svg"
                            class="w-6 h-6 mx-auto mb-2"
                            width="24"
                            height="24"
                            alt="" />
                          <span class="h5">Behance</span>
                        </a>
                      </div>
                      <div class="col-4">
                        <a
                          href="#"
                          class="d-flex flex-column flex-center text-center text-secondary py-2 px-2 link-hoverable">
                          <img
                            src="./static/brands/discord.svg"
                            class="w-6 h-6 mx-auto mb-2"
                            width="24"
                            height="24"
                            alt="" />
                          <span class="h5">Discord</span>
                        </a>
                      </div>
                      <div class="col-4">
                        <a
                          href="#"
                          class="d-flex flex-column flex-center text-center text-secondary py-2 px-2 link-hoverable">
                          <img
                            src="./static/brands/dribbble.svg"
                            class="w-6 h-6 mx-auto mb-2"
                            width="24"
                            height="24"
                            alt="" />
                          <span class="h5">Dribbble</span>
                        </a>
                      </div>
                      <div class="col-4">
                        <a
                          href="#"
                          class="d-flex flex-column flex-center text-center text-secondary py-2 px-2 link-hoverable">
                          <img
                            src="./static/brands/dropbox.svg"
                            class="w-6 h-6 mx-auto mb-2"
                            width="24"
                            height="24"
                            alt="" />
                          <span class="h5">Dropbox</span>
                        </a>
                      </div>
                      <div class="col-4">
                        <a
                          href="#"
                          class="d-flex flex-column flex-center text-center text-secondary py-2 px-2 link-hoverable">
                          <img
                            src="./static/brands/ever-green.svg"
                            class="w-6 h-6 mx-auto mb-2"
                            width="24"
                            height="24"
                            alt="" />
                          <span class="h5">Ever Green</span>
                        </a>
                      </div>
                      <div class="col-4">
                        <a
                          href="#"
                          class="d-flex flex-column flex-center text-center text-secondary py-2 px-2 link-hoverable">
                          <img
                            src="./static/brands/facebook.svg"
                            class="w-6 h-6 mx-auto mb-2"
                            width="24"
                            height="24"
                            alt="" />
                          <span class="h5">Facebook</span>
                        </a>
                      </div>
                      <div class="col-4">
                        <a
                          href="#"
                          class="d-flex flex-column flex-center text-center text-secondary py-2 px-2 link-hoverable">
                          <img
                            src="./static/brands/figma.svg"
                            class="w-6 h-6 mx-auto mb-2"
                            width="24"
                            height="24"
                            alt="" />
                          <span class="h5">Figma</span>
                        </a>
                      </div>
                      <div class="col-4">
                        <a
                          href="#"
                          class="d-flex flex-column flex-center text-center text-secondary py-2 px-2 link-hoverable">
                          <img
                            src="./static/brands/github.svg"
                            class="w-6 h-6 mx-auto mb-2"
                            width="24"
                            height="24"
                            alt="" />
                          <span class="h5">GitHub</span>
                        </a>
                      </div>
                      <div class="col-4">
                        <a
                          href="#"
                          class="d-flex flex-column flex-center text-center text-secondary py-2 px-2 link-hoverable">
                          <img
                            src="./static/brands/gitlab.svg"
                            class="w-6 h-6 mx-auto mb-2"
                            width="24"
                            height="24"
                            alt="" />
                          <span class="h5">GitLab</span>
                        </a>
                      </div>
                      <div class="col-4">
                        <a
                          href="#"
                          class="d-flex flex-column flex-center text-center text-secondary py-2 px-2 link-hoverable">
                          <img
                            src="./static/brands/google-ads.svg"
                            class="w-6 h-6 mx-auto mb-2"
                            width="24"
                            height="24"
                            alt="" />
                          <span class="h5">Google Ads</span>
                        </a>
                      </div>
                      <div class="col-4">
                        <a
                          href="#"
                          class="d-flex flex-column flex-center text-center text-secondary py-2 px-2 link-hoverable">
                          <img
                            src="./static/brands/google-adsense.svg"
                            class="w-6 h-6 mx-auto mb-2"
                            width="24"
                            height="24"
                            alt="" />
                          <span class="h5">Google AdSense</span>
                        </a>
                      </div>
                      <div class="col-4">
                        <a
                          href="#"
                          class="d-flex flex-column flex-center text-center text-secondary py-2 px-2 link-hoverable">
                          <img
                            src="./static/brands/google-analytics.svg"
                            class="w-6 h-6 mx-auto mb-2"
                            width="24"
                            height="24"
                            alt="" />
                          <span class="h5">Google Analytics</span>
                        </a>
                      </div>
                      <div class="col-4">
                        <a
                          href="#"
                          class="d-flex flex-column flex-center text-center text-secondary py-2 px-2 link-hoverable">
                          <img
                            src="./static/brands/google-cloud.svg"
                            class="w-6 h-6 mx-auto mb-2"
                            width="24"
                            height="24"
                            alt="" />
                          <span class="h5">Google Cloud</span>
                        </a>
                      </div>
                      <div class="col-4">
                        <a
                          href="#"
                          class="d-flex flex-column flex-center text-center text-secondary py-2 px-2 link-hoverable">
                          <img
                            src="./static/brands/google-drive.svg"
                            class="w-6 h-6 mx-auto mb-2"
                            width="24"
                            height="24"
                            alt="" />
                          <span class="h5">Google Drive</span>
                        </a>
                      </div>
                      <div class="col-4">
                        <a
                          href="#"
                          class="d-flex flex-column flex-center text-center text-secondary py-2 px-2 link-hoverable">
                          <img
                            src="./static/brands/google-fit.svg"
                            class="w-6 h-6 mx-auto mb-2"
                            width="24"
                            height="24"
                            alt="" />
                          <span class="h5">Google Fit</span>
                        </a>
                      </div>
                      <div class="col-4">
                        <a
                          href="#"
                          class="d-flex flex-column flex-center text-center text-secondary py-2 px-2 link-hoverable">
                          <img
                            src="./static/brands/google-home.svg"
                            class="w-6 h-6 mx-auto mb-2"
                            width="24"
                            height="24"
                            alt="" />
                          <span class="h5">Google Home</span>
                        </a>
                      </div>
                      <div class="col-4">
                        <a
                          href="#"
                          class="d-flex flex-column flex-center text-center text-secondary py-2 px-2 link-hoverable">
                          <img
                            src="./static/brands/google-maps.svg"
                            class="w-6 h-6 mx-auto mb-2"
                            width="24"
                            height="24"
                            alt="" />
                          <span class="h5">Google Maps</span>
                        </a>
                      </div>
                      <div class="col-4">
                        <a
                          href="#"
                          class="d-flex flex-column flex-center text-center text-secondary py-2 px-2 link-hoverable">
                          <img
                            src="./static/brands/google-meet.svg"
                            class="w-6 h-6 mx-auto mb-2"
                            width="24"
                            height="24"
                            alt="" />
                          <span class="h5">Google Meet</span>
                        </a>
                      </div>
                      <div class="col-4">
                        <a
                          href="#"
                          class="d-flex flex-column flex-center text-center text-secondary py-2 px-2 link-hoverable">
                          <img
                            src="./static/brands/google-photos.svg"
                            class="w-6 h-6 mx-auto mb-2"
                            width="24"
                            height="24"
                            alt="" />
                          <span class="h5">Google Photos</span>
                        </a>
                      </div>
                      <div class="col-4">
                        <a
                          href="#"
                          class="d-flex flex-column flex-center text-center text-secondary py-2 px-2 link-hoverable">
                          <img
                            src="./static/brands/google-play.svg"
                            class="w-6 h-6 mx-auto mb-2"
                            width="24"
                            height="24"
                            alt="" />
                          <span class="h5">Google Play</span>
                        </a>
                      </div>
                      <div class="col-4">
                        <a
                          href="#"
                          class="d-flex flex-column flex-center text-center text-secondary py-2 px-2 link-hoverable">
                          <img
                            src="./static/brands/google-shopping.svg"
                            class="w-6 h-6 mx-auto mb-2"
                            width="24"
                            height="24"
                            alt="" />
                          <span class="h5">Google Shopping</span>
                        </a>
                      </div>
                      <div class="col-4">
                        <a
                          href="#"
                          class="d-flex flex-column flex-center text-center text-secondary py-2 px-2 link-hoverable">
                          <img
                            src="./static/brands/google-teams.svg"
                            class="w-6 h-6 mx-auto mb-2"
                            width="24"
                            height="24"
                            alt="" />
                          <span class="h5">Google Teams</span>
                        </a>
                      </div>
                      <div class="col-4">
                        <a
                          href="#"
                          class="d-flex flex-column flex-center text-center text-secondary py-2 px-2 link-hoverable">
                          <img
                            src="./static/brands/google.svg"
                            class="w-6 h-6 mx-auto mb-2"
                            width="24"
                            height="24"
                            alt="" />
                          <span class="h5">Google</span>
                        </a>
                      </div>
                      <div class="col-4">
                        <a
                          href="#"
                          class="d-flex flex-column flex-center text-center text-secondary py-2 px-2 link-hoverable">
                          <img
                            src="./static/brands/instagram.svg"
                            class="w-6 h-6 mx-auto mb-2"
                            width="24"
                            height="24"
                            alt="" />
                          <span class="h5">Instagram</span>
                        </a>
                      </div>
                      <div class="col-4">
                        <a
                          href="#"
                          class="d-flex flex-column flex-center text-center text-secondary py-2 px-2 link-hoverable">
                          <img
                            src="./static/brands/klarna.svg"
                            class="w-6 h-6 mx-auto mb-2"
                            width="24"
                            height="24"
                            alt="" />
                          <span class="h5">Klarna</span>
                        </a>
                      </div>
                      <div class="col-4">
                        <a
                          href="#"
                          class="d-flex flex-column flex-center text-center text-secondary py-2 px-2 link-hoverable">
                          <img
                            src="./static/brands/linkedin.svg"
                            class="w-6 h-6 mx-auto mb-2"
                            width="24"
                            height="24"
                            alt="" />
                          <span class="h5">LinkedIn</span>
                        </a>
                      </div>
                      <div class="col-4">
                        <a
                          href="#"
                          class="d-flex flex-column flex-center text-center text-secondary py-2 px-2 link-hoverable">
                          <img
                            src="./static/brands/mailchimp.svg"
                            class="w-6 h-6 mx-auto mb-2"
                            width="24"
                            height="24"
                            alt="" />
                          <span class="h5">Mailchimp</span>
                        </a>
                      </div>
                      <div class="col-4">
                        <a
                          href="#"
                          class="d-flex flex-column flex-center text-center text-secondary py-2 px-2 link-hoverable">
                          <img
                            src="./static/brands/medium.svg"
                            class="w-6 h-6 mx-auto mb-2"
                            width="24"
                            height="24"
                            alt="" />
                          <span class="h5">Medium</span>
                        </a>
                      </div>
                      <div class="col-4">
                        <a
                          href="#"
                          class="d-flex flex-column flex-center text-center text-secondary py-2 px-2 link-hoverable">
                          <img
                            src="./static/brands/messenger.svg"
                            class="w-6 h-6 mx-auto mb-2"
                            width="24"
                            height="24"
                            alt="" />
                          <span class="h5">Messenger</span>
                        </a>
                      </div>
                      <div class="col-4">
                        <a
                          href="#"
                          class="d-flex flex-column flex-center text-center text-secondary py-2 px-2 link-hoverable">
                          <img
                            src="./static/brands/meta.svg"
                            class="w-6 h-6 mx-auto mb-2"
                            width="24"
                            height="24"
                            alt="" />
                          <span class="h5">Meta</span>
                        </a>
                      </div>
                      <div class="col-4">
                        <a
                          href="#"
                          class="d-flex flex-column flex-center text-center text-secondary py-2 px-2 link-hoverable">
                          <img
                            src="./static/brands/monday.svg"
                            class="w-6 h-6 mx-auto mb-2"
                            width="24"
                            height="24"
                            alt="" />
                          <span class="h5">Monday</span>
                        </a>
                      </div>
                      <div class="col-4">
                        <a
                          href="#"
                          class="d-flex flex-column flex-center text-center text-secondary py-2 px-2 link-hoverable">
                          <img
                            src="./static/brands/netflix.svg"
                            class="w-6 h-6 mx-auto mb-2"
                            width="24"
                            height="24"
                            alt="" />
                          <span class="h5">Netflix</span>
                        </a>
                      </div>
                      <div class="col-4">
                        <a
                          href="#"
                          class="d-flex flex-column flex-center text-center text-secondary py-2 px-2 link-hoverable">
                          <img
                            src="./static/brands/notion.svg"
                            class="w-6 h-6 mx-auto mb-2"
                            width="24"
                            height="24"
                            alt="" />
                          <span class="h5">Notion</span>
                        </a>
                      </div>
                      <div class="col-4">
                        <a
                          href="#"
                          class="d-flex flex-column flex-center text-center text-secondary py-2 px-2 link-hoverable">
                          <img
                            src="./static/brands/office-365.svg"
                            class="w-6 h-6 mx-auto mb-2"
                            width="24"
                            height="24"
                            alt="" />
                          <span class="h5">Office 365</span>
                        </a>
                      </div>
                      <div class="col-4">
                        <a
                          href="#"
                          class="d-flex flex-column flex-center text-center text-secondary py-2 px-2 link-hoverable">
                          <img
                            src="./static/brands/opera.svg"
                            class="w-6 h-6 mx-auto mb-2"
                            width="24"
                            height="24"
                            alt="" />
                          <span class="h5">Opera</span>
                        </a>
                      </div>
                      <div class="col-4">
                        <a
                          href="#"
                          class="d-flex flex-column flex-center text-center text-secondary py-2 px-2 link-hoverable">
                          <img
                            src="./static/brands/paypal.svg"
                            class="w-6 h-6 mx-auto mb-2"
                            width="24"
                            height="24"
                            alt="" />
                          <span class="h5">PayPal</span>
                        </a>
                      </div>
                      <div class="col-4">
                        <a
                          href="#"
                          class="d-flex flex-column flex-center text-center text-secondary py-2 px-2 link-hoverable">
                          <img
                            src="./static/brands/petreon.svg"
                            class="w-6 h-6 mx-auto mb-2"
                            width="24"
                            height="24"
                            alt="" />
                          <span class="h5">Patreon</span>
                        </a>
                      </div>
                      <div class="col-4">
                        <a
                          href="#"
                          class="d-flex flex-column flex-center text-center text-secondary py-2 px-2 link-hoverable">
                          <img
                            src="./static/brands/pinterest.svg"
                            class="w-6 h-6 mx-auto mb-2"
                            width="24"
                            height="24"
                            alt="" />
                          <span class="h5">Pinterest</span>
                        </a>
                      </div>
                      <div class="col-4">
                        <a
                          href="#"
                          class="d-flex flex-column flex-center text-center text-secondary py-2 px-2 link-hoverable">
                          <img
                            src="./static/brands/play-store.svg"
                            class="w-6 h-6 mx-auto mb-2"
                            width="24"
                            height="24"
                            alt="" />
                          <span class="h5">Play Store</span>
                        </a>
                      </div>
                      <div class="col-4">
                        <a
                          href="#"
                          class="d-flex flex-column flex-center text-center text-secondary py-2 px-2 link-hoverable">
                          <img
                            src="./static/brands/quora.svg"
                            class="w-6 h-6 mx-auto mb-2"
                            width="24"
                            height="24"
                            alt="" />
                          <span class="h5">Quora</span>
                        </a>
                      </div>
                      <div class="col-4">
                        <a
                          href="#"
                          class="d-flex flex-column flex-center text-center text-secondary py-2 px-2 link-hoverable">
                          <img
                            src="./static/brands/reddit.svg"
                            class="w-6 h-6 mx-auto mb-2"
                            width="24"
                            height="24"
                            alt="" />
                          <span class="h5">Reddit</span>
                        </a>
                      </div>
                      <div class="col-4">
                        <a
                          href="#"
                          class="d-flex flex-column flex-center text-center text-secondary py-2 px-2 link-hoverable">
                          <img
                            src="./static/brands/shopify.svg"
                            class="w-6 h-6 mx-auto mb-2"
                            width="24"
                            height="24"
                            alt="" />
                          <span class="h5">Shopify</span>
                        </a>
                      </div>
                      <div class="col-4">
                        <a
                          href="#"
                          class="d-flex flex-column flex-center text-center text-secondary py-2 px-2 link-hoverable">
                          <img
                            src="./static/brands/skype.svg"
                            class="w-6 h-6 mx-auto mb-2"
                            width="24"
                            height="24"
                            alt="" />
                          <span class="h5">Skype</span>
                        </a>
                      </div>
                      <div class="col-4">
                        <a
                          href="#"
                          class="d-flex flex-column flex-center text-center text-secondary py-2 px-2 link-hoverable">
                          <img
                            src="./static/brands/slack.svg"
                            class="w-6 h-6 mx-auto mb-2"
                            width="24"
                            height="24"
                            alt="" />
                          <span class="h5">Slack</span>
                        </a>
                      </div>
                      <div class="col-4">
                        <a
                          href="#"
                          class="d-flex flex-column flex-center text-center text-secondary py-2 px-2 link-hoverable">
                          <img
                            src="./static/brands/snapchat.svg"
                            class="w-6 h-6 mx-auto mb-2"
                            width="24"
                            height="24"
                            alt="" />
                          <span class="h5">Snapchat</span>
                        </a>
                      </div>
                      <div class="col-4">
                        <a
                          href="#"
                          class="d-flex flex-column flex-center text-center text-secondary py-2 px-2 link-hoverable">
                          <img
                            src="./static/brands/soundcloud.svg"
                            class="w-6 h-6 mx-auto mb-2"
                            width="24"
                            height="24"
                            alt="" />
                          <span class="h5">SoundCloud</span>
                        </a>
                      </div>
                      <div class="col-4">
                        <a
                          href="#"
                          class="d-flex flex-column flex-center text-center text-secondary py-2 px-2 link-hoverable">
                          <img
                            src="./static/brands/spotify.svg"
                            class="w-6 h-6 mx-auto mb-2"
                            width="24"
                            height="24"
                            alt="" />
                          <span class="h5">Spotify</span>
                        </a>
                      </div>
                      <div class="col-4">
                        <a
                          href="#"
                          class="d-flex flex-column flex-center text-center text-secondary py-2 px-2 link-hoverable">
                          <img
                            src="./static/brands/stripe.svg"
                            class="w-6 h-6 mx-auto mb-2"
                            width="24"
                            height="24"
                            alt="" />
                          <span class="h5">Stripe</span>
                        </a>
                      </div>
                      <div class="col-4">
                        <a
                          href="#"
                          class="d-flex flex-column flex-center text-center text-secondary py-2 px-2 link-hoverable">
                          <img
                            src="./static/brands/telegram.svg"
                            class="w-6 h-6 mx-auto mb-2"
                            width="24"
                            height="24"
                            alt="" />
                          <span class="h5">Telegram</span>
                        </a>
                      </div>
                      <div class="col-4">
                        <a
                          href="#"
                          class="d-flex flex-column flex-center text-center text-secondary py-2 px-2 link-hoverable">
                          <img
                            src="./static/brands/tiktok.svg"
                            class="w-6 h-6 mx-auto mb-2"
                            width="24"
                            height="24"
                            alt="" />
                          <span class="h5">TikTok</span>
                        </a>
                      </div>
                      <div class="col-4">
                        <a
                          href="#"
                          class="d-flex flex-column flex-center text-center text-secondary py-2 px-2 link-hoverable">
                          <img
                            src="./static/brands/tinder.svg"
                            class="w-6 h-6 mx-auto mb-2"
                            width="24"
                            height="24"
                            alt="" />
                          <span class="h5">Tinder</span>
                        </a>
                      </div>
                      <div class="col-4">
                        <a
                          href="#"
                          class="d-flex flex-column flex-center text-center text-secondary py-2 px-2 link-hoverable">
                          <img
                            src="./static/brands/trello.svg"
                            class="w-6 h-6 mx-auto mb-2"
                            width="24"
                            height="24"
                            alt="" />
                          <span class="h5">Trello</span>
                        </a>
                      </div>
                      <div class="col-4">
                        <a
                          href="#"
                          class="d-flex flex-column flex-center text-center text-secondary py-2 px-2 link-hoverable">
                          <img
                            src="./static/brands/truth.svg"
                            class="w-6 h-6 mx-auto mb-2"
                            width="24"
                            height="24"
                            alt="" />
                          <span class="h5">Truth</span>
                        </a>
                      </div>
                      <div class="col-4">
                        <a
                          href="#"
                          class="d-flex flex-column flex-center text-center text-secondary py-2 px-2 link-hoverable">
                          <img
                            src="./static/brands/tumblr.svg"
                            class="w-6 h-6 mx-auto mb-2"
                            width="24"
                            height="24"
                            alt="" />
                          <span class="h5">Tumblr</span>
                        </a>
                      </div>
                      <div class="col-4">
                        <a
                          href="#"
                          class="d-flex flex-column flex-center text-center text-secondary py-2 px-2 link-hoverable">
                          <img
                            src="./static/brands/twitch.svg"
                            class="w-6 h-6 mx-auto mb-2"
                            width="24"
                            height="24"
                            alt="" />
                          <span class="h5">Twitch</span>
                        </a>
                      </div>
                      <div class="col-4">
                        <a
                          href="#"
                          class="d-flex flex-column flex-center text-center text-secondary py-2 px-2 link-hoverable">
                          <img
                            src="./static/brands/twitter.svg"
                            class="w-6 h-6 mx-auto mb-2"
                            width="24"
                            height="24"
                            alt="" />
                          <span class="h5">Twitter</span>
                        </a>
                      </div>
                      <div class="col-4">
                        <a
                          href="#"
                          class="d-flex flex-column flex-center text-center text-secondary py-2 px-2 link-hoverable">
                          <img
                            src="./static/brands/vimeo.svg"
                            class="w-6 h-6 mx-auto mb-2"
                            width="24"
                            height="24"
                            alt="" />
                          <span class="h5">Vimeo</span>
                        </a>
                      </div>
                      <div class="col-4">
                        <a
                          href="#"
                          class="d-flex flex-column flex-center text-center text-secondary py-2 px-2 link-hoverable">
                          <img
                            src="./static/brands/vk.svg"
                            class="w-6 h-6 mx-auto mb-2"
                            width="24"
                            height="24"
                            alt="" />
                          <span class="h5">VK</span>
                        </a>
                      </div>
                      <div class="col-4">
                        <a
                          href="#"
                          class="d-flex flex-column flex-center text-center text-secondary py-2 px-2 link-hoverable">
                          <img
                            src="./static/brands/watppad.svg"
                            class="w-6 h-6 mx-auto mb-2"
                            width="24"
                            height="24"
                            alt="" />
                          <span class="h5">Wattpad</span>
                        </a>
                      </div>
                      <div class="col-4">
                        <a
                          href="#"
                          class="d-flex flex-column flex-center text-center text-secondary py-2 px-2 link-hoverable">
                          <img
                            src="./static/brands/webflow.svg"
                            class="w-6 h-6 mx-auto mb-2"
                            width="24"
                            height="24"
                            alt="" />
                          <span class="h5">Webflow</span>
                        </a>
                      </div>
                      <div class="col-4">
                        <a
                          href="#"
                          class="d-flex flex-column flex-center text-center text-secondary py-2 px-2 link-hoverable">
                          <img
                            src="./static/brands/whatsapp.svg"
                            class="w-6 h-6 mx-auto mb-2"
                            width="24"
                            height="24"
                            alt="" />
                          <span class="h5">WhatsApp</span>
                        </a>
                      </div>
                      <div class="col-4">
                        <a
                          href="#"
                          class="d-flex flex-column flex-center text-center text-secondary py-2 px-2 link-hoverable">
                          <img
                            src="./static/brands/wordpress.svg"
                            class="w-6 h-6 mx-auto mb-2"
                            width="24"
                            height="24"
                            alt="" />
                          <span class="h5">WordPress</span>
                        </a>
                      </div>
                      <div class="col-4">
                        <a
                          href="#"
                          class="d-flex flex-column flex-center text-center text-secondary py-2 px-2 link-hoverable">
                          <img
                            src="./static/brands/xing.svg"
                            class="w-6 h-6 mx-auto mb-2"
                            width="24"
                            height="24"
                            alt="" />
                          <span class="h5">Xing</span>
                        </a>
                      </div>
                      <div class="col-4">
                        <a
                          href="#"
                          class="d-flex flex-column flex-center text-center text-secondary py-2 px-2 link-hoverable">
                          <img
                            src="./static/brands/yelp.svg"
                            class="w-6 h-6 mx-auto mb-2"
                            width="24"
                            height="24"
                            alt="" />
                          <span class="h5">Yelp</span>
                        </a>
                      </div>
                      <div class="col-4">
                        <a
                          href="#"
                          class="d-flex flex-column flex-center text-center text-secondary py-2 px-2 link-hoverable">
                          <img
                            src="./static/brands/youtube.svg"
                            class="w-6 h-6 mx-auto mb-2"
                            width="24"
                            height="24"
                            alt="" />
                          <span class="h5">YouTube</span>
                        </a>
                      </div>
                      <div class="col-4">
                        <a
                          href="#"
                          class="d-flex flex-column flex-center text-center text-secondary py-2 px-2 link-hoverable">
                          <img
                            src="./static/brands/zapier.svg"
                            class="w-6 h-6 mx-auto mb-2"
                            width="24"
                            height="24"
                            alt="" />
                          <span class="h5">Zapier</span>
                        </a>
                      </div>
                      <div class="col-4">
                        <a
                          href="#"
                          class="d-flex flex-column flex-center text-center text-secondary py-2 px-2 link-hoverable">
                          <img
                            src="./static/brands/zendesk.svg"
                            class="w-6 h-6 mx-auto mb-2"
                            width="24"
                            height="24"
                            alt="" />
                          <span class="h5">Zendesk</span>
                        </a>
                      </div>
                      <div class="col-4">
                        <a
                          href="#"
                          class="d-flex flex-column flex-center text-center text-secondary py-2 px-2 link-hoverable">
                          <img
                            src="./static/brands/zoom.svg"
                            class="w-6 h-6 mx-auto mb-2"
                            width="24"
                            height="24"
                            alt="" />
                          <span class="h5">Zoom</span>
                        </a>
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
              class="nav-link d-flex lh-1 p-0 px-2"
              data-bs-toggle="dropdown"
              aria-label="Open user menu">
              <span class="avatar avatar-sm" [style]="userAvatar()"> </span>
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
    <header class="navbar-expand-md">
      <div class="navbar-collapse collapse show" id="navbar-menu" style="">
        <div class="navbar">
          <div class="container-xl">
            <div class="row flex-column flex-md-row flex-fill align-items-center">
              <div class="col">
                <!-- BEGIN NAVBAR MENU -->
                <ul class="navbar-nav">
                  <li class="nav-item active">
                    <a class="nav-link" href="./#">
                      <span class="nav-link-icon d-md-none d-lg-inline-block"
                        ><!-- Download SVG icon from http://tabler.io/icons/icon/star -->
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
                          class="icon icon-1">
                          <path
                            d="M12 17.75l-6.172 3.245l1.179 -6.873l-5 -4.867l6.9 -1l3.086 -6.253l3.086 6.253l6.9 1l-5 4.867l1.179 6.873z"></path>
                        </svg>
                      </span>
                      <span class="nav-link-title"> First </span>
                    </a>
                  </li>
                  <li class="nav-item">
                    <a class="nav-link" href="./#">
                      <span class="nav-link-icon d-md-none d-lg-inline-block"
                        ><!-- Download SVG icon from http://tabler.io/icons/icon/star -->
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
                          class="icon icon-1">
                          <path
                            d="M12 17.75l-6.172 3.245l1.179 -6.873l-5 -4.867l6.9 -1l3.086 -6.253l3.086 6.253l6.9 1l-5 4.867l1.179 6.873z"></path>
                        </svg>
                      </span>
                      <span class="nav-link-title"> Second </span>
                      <span class="badge badge-sm bg-red text-red-fg">2</span>
                    </a>
                  </li>
                  <li class="nav-item dropdown">
                    <a
                      class="nav-link dropdown-toggle"
                      href="#navbar-third"
                      data-bs-toggle="dropdown"
                      data-bs-auto-close="outside"
                      role="button"
                      aria-expanded="false">
                      <span class="nav-link-icon d-md-none d-lg-inline-block"
                        ><!-- Download SVG icon from http://tabler.io/icons/icon/star -->
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
                          class="icon icon-1">
                          <path
                            d="M12 17.75l-6.172 3.245l1.179 -6.873l-5 -4.867l6.9 -1l3.086 -6.253l3.086 6.253l6.9 1l-5 4.867l1.179 6.873z"></path>
                        </svg>
                      </span>
                      <span class="nav-link-title"> Third </span>
                    </a>
                    <div class="dropdown-menu">
                      <a class="dropdown-item" href="./#"> First </a>
                      <a class="dropdown-item" href="./#"> Second </a>
                      <a class="dropdown-item" href="./#"> Third </a>
                    </div>
                  </li>
                  <li class="nav-item">
                    <a class="nav-link disabled" href="./#">
                      <span class="nav-link-icon d-md-none d-lg-inline-block"
                        ><!-- Download SVG icon from http://tabler.io/icons/icon/star -->
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
                          class="icon icon-1">
                          <path
                            d="M12 17.75l-6.172 3.245l1.179 -6.873l-5 -4.867l6.9 -1l3.086 -6.253l3.086 6.253l6.9 1l-5 4.867l1.179 6.873z"></path>
                        </svg>
                      </span>
                      <span class="nav-link-title"> Disabled </span>
                    </a>
                  </li>
                </ul>
                <!-- END NAVBAR MENU -->
              </div>
              <div class="col-2 d-none d-xxl-block">
                <div class="my-2 my-md-0 flex-grow-1 flex-md-grow-0 order-first order-md-last">
                  <form action="./" method="get" autocomplete="off" novalidate="">
                    <div class="input-icon">
                      <span class="input-icon-addon">
                        <!-- Download SVG icon from http://tabler.io/icons/icon/search -->
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
                          class="icon icon-1">
                          <path d="M10 10m-7 0a7 7 0 1 0 14 0a7 7 0 1 0 -14 0"></path>
                          <path d="M21 21l-6 -6"></path>
                        </svg>
                      </span>
                      <input
                        type="text"
                        value=""
                        class="form-control"
                        placeholder="Search…"
                        aria-label="Search in website" />
                    </div>
                  </form>
                </div>
              </div>
              <div class="col col-md-auto">
                <ul class="navbar-nav">
                  <li class="nav-item">
                    <a
                      class="nav-link"
                      href="#"
                      data-bs-toggle="offcanvas"
                      data-bs-target="#offcanvasSettings">
                      <span class="badge badge-sm bg-red text-red-fg">New</span>
                      <span class="nav-link-icon d-md-none d-lg-inline-block">
                        <!-- Download SVG icon from http://tabler.io/icons/icon/settings -->
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
                          class="icon icon-1">
                          <path
                            d="M10.325 4.317c.426 -1.756 2.924 -1.756 3.35 0a1.724 1.724 0 0 0 2.573 1.066c1.543 -.94 3.31 .826 2.37 2.37a1.724 1.724 0 0 0 1.065 2.572c1.756 .426 1.756 2.924 0 3.35a1.724 1.724 0 0 0 -1.066 2.573c.94 1.543 -.826 3.31 -2.37 2.37a1.724 1.724 0 0 0 -2.572 1.065c-.426 1.756 -2.924 1.756 -3.35 0a1.724 1.724 0 0 0 -2.573 -1.066c-1.543 .94 -3.31 -.826 -2.37 -2.37a1.724 1.724 0 0 0 -1.065 -2.572c-1.756 -.426 -1.756 -2.924 0 -3.35a1.724 1.724 0 0 0 1.066 -2.573c-.94 -1.543 .826 -3.31 2.37 -2.37c1 .608 2.296 .07 2.572 -1.065z"></path>
                          <path d="M9 12a3 3 0 1 0 6 0a3 3 0 0 0 -6 0"></path>
                        </svg>
                      </span>
                      <span class="nav-link-title"> Theme Settings </span>
                    </a>
                  </li>
                </ul>
              </div>
            </div>
          </div>
        </div>
      </div>
    </header>
    <div class="page-wrapper">
      <div class="container-xxl">
        <div class="page-header d-print-none">
          <div class="row align-items-center">
            <div class="col">
              <div class="page-pretitle">Overview</div>
              <h2 class="page-title">Dashboard</h2>
            </div>
            <div class="col-auto ms-auto">
              <div class="btn-list">
                <span class="d-none d-sm-inline">
                  <a href="#" class="btn"> New view </a>
                </span>
                <a
                  href="#"
                  class="btn btn-primary d-none d-sm-inline-block"
                  data-bs-toggle="modal"
                  data-bs-target="#modal-report">
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
                    stroke-linejoin="round">
                    <path stroke="none" d="M0 0h24v24H0z" fill="none" />
                    <line x1="12" y1="5" x2="12" y2="19" />
                    <line x1="5" y1="12" x2="19" y2="12" />
                  </svg>
                  Create new report
                </a>
                <a
                  href="#"
                  class="btn btn-primary d-sm-none btn-icon"
                  data-bs-toggle="modal"
                  data-bs-target="#modal-report"
                  aria-label="Create new report">
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
                    stroke-linejoin="round">
                    <path stroke="none" d="M0 0h24v24H0z" fill="none" />
                    <line x1="12" y1="5" x2="12" y2="19" />
                    <line x1="5" y1="12" x2="19" y2="12" />
                  </svg>
                </a>
              </div>
            </div>
          </div>
        </div>
        <div class="page-body"><router-outlet></router-outlet></div>
      </div>
    </div>
  </div>`,
  styles: [
    `
      :host {
        min-height: 100%;
        min-width: 100%;
      }
      avatar {
        background-image: url('/assets/img/avater.png');
      }
    `,
  ],
})
export class BaseLayout implements OnInit {
  private readonly _tokenSer = inject(TokenService);

  userDetails = signal({} as UserDetails);
  userAvatar = signal({
    backgroundImage: "url('assets/img/avater.png')",
  });

  constructor() {
    afterNextRender(() => {
      this._tokenSer.isLoggedIn$.subscribe(isLoggedIn => {
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
  ngOnInit(): void {}
}

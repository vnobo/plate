import { Component, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { LayoutAside } from './layout-aside';
import { LayoutHeader } from './layout-header';
import { LayoutService } from './layout.service';

@Component({
  selector: 'app-layout-base',
  imports: [RouterOutlet, LayoutHeader, LayoutAside],
  template: `
    <a class="visually-hidden-focusable" href="#page-body">Skip to main content</a>
    <div class="page">
      <aside layout-aside></aside>
      <header layout-header></header>
      @if (layout.mobileOpen()) {
        <!-- d-lg-none: once the breakpoint widens, the backdrop is not left behind even if mobileOpen stays true -->
        <div class="offcanvas-backdrop fade show d-lg-none" (click)="layout.closeMobile()"></div>
      }
      <div class="page-wrapper">
        <main id="page-body" class="page-body">
          <div class="container-fluid">
            <router-outlet></router-outlet>
          </div>
        </main>
        <footer class="footer footer-transparent d-print-none">
          <div class="container-fluid">
            <div class="row text-center align-items-center flex-column flex-sm-row">
              <div class="col-12 col-sm-6 text-sm-start">Plate Console</div>
              <div class="col-12 col-sm-6 text-sm-end text-secondary">Copyright © 2026</div>
            </div>
          </div>
        </footer>
      </div>
    </div>
  `,
  styles: `
    :host {
      display: block;
    }
  `,
})
export class BaseLayout {
  readonly layout = inject(LayoutService);
}

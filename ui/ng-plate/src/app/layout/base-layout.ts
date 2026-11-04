import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { RouterOutlet } from '@angular/router';
import { LayoutAside } from './layout-aside';
import { LayoutHeader } from './layout-header';
import { LayoutService } from './layout.service';
import { ProgressService } from '@app/plugins/progress';

@Component({
  selector: 'app-layout-base',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterOutlet, LayoutHeader, LayoutAside],
  template: `
    <a class="visually-hidden-focusable" href="#page-body">跳到主要内容</a>
    @if (showLoading()) {
      <div class="global-loading">
        <div class="spinner-border text-primary" role="status">
          <span class="visually-hidden">加载中…</span>
        </div>
      </div>
    }
    <div class="page">
      <aside layout-aside></aside>
      <header layout-header></header>
      @if (layout.mobileOpen()) {
        <!-- d-lg-none：断点变宽后即使 mobileOpen 仍为 true 也不残留遮罩 -->
        <div class="offcanvas-backdrop fade show d-lg-none" (click)="layout.closeMobile()"></div>
      }
      <div class="page-wrapper">
        <main id="page-body" class="page-body">
          <div class="container-xl">
            <router-outlet></router-outlet>
          </div>
        </main>
        <footer class="footer footer-transparent d-print-none">
          <div class="container-xl">
            <div class="row text-center align-items-center flex-column flex-sm-row">
              <div class="col-12 col-sm-6 text-sm-start">Plate 控制台</div>
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

    .global-loading {
      position: fixed;
      inset: 0;
      z-index: 1080;
      display: flex;
      align-items: center;
      justify-content: center;
      background-color: rgba(255, 255, 255, 0.55);
      pointer-events: all;
    }

    .global-loading .spinner-border {
      width: 2.5rem;
      height: 2.5rem;
    }
  `,
})
export class BaseLayout {
  readonly layout = inject(LayoutService);
  private readonly progress = inject(ProgressService);
  readonly showLoading = toSignal(this.progress.isShow$, { initialValue: false });
}

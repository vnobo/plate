import { Component } from '@angular/core';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-layout-blank',
  imports: [RouterModule],
  template: `
    <div class="page">
      <div class="page-wrapper">
        <div class="page-body">
          <router-outlet></router-outlet>
        </div>
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
  styles: [
    `
      :host {
        min-height: 100vh;
        min-width: 100%;
        display: block;
      }

      .page {
        min-height: 100vh;
        background-color: var(--tblr-bg-surface);
      }

      .page-wrapper {
        min-height: 100vh;
        display: flex;
        flex-direction: column;
      }

      .page-body {
        flex: 1;
        padding: 0;
      }
    `,
  ],
})
export class BlankLayout {}

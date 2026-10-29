import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { LayoutAside } from './layout-aside';
import { LayoutHeader } from './layout-header';

@Component({
  selector: 'app-layout-base',
  imports: [RouterOutlet, LayoutHeader, LayoutAside],
  template: `
    <div class="page">
      <aside layout-aside></aside>
      <header layout-header></header>
      <div class="page-wrapper">
        <div class="page-body">
          <div class="container-xl">
            <router-outlet></router-outlet>
          </div>
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
  styles: `
    :host {
      display: block;
    }
  `,
})
export class BaseLayout {}

import { afterNextRender, Component, OnDestroy } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { LayoutAside } from './layout-aside';
import { LayoutHeader } from './layout-header';

@Component({
  selector: 'app-layout-base',
  imports: [RouterOutlet, LayoutHeader, LayoutAside],
  template: `
    <layout-aside class="layout-aside"></layout-aside>
    <div class="layout-content">
      <layout-header></layout-header>
      <div class="page-wrapper">
        <div class="page-body">
          <div class="container-xl">
            <router-outlet></router-outlet>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: `
    :host {
      display: contents;
    }
  `,
  host: {
    width: '100%',
    height: '100%',
    class: 'page',
  },
})
export class BaseLayout implements OnDestroy {
  constructor() {
    afterNextRender(() => {
      document.body.classList.add('layout-fluid');
    });
  }

  ngOnDestroy(): void {
    document.body.classList.remove('layout-fluid');
  }
}

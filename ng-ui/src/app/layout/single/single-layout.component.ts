import { Component, type OnInit } from '@angular/core';
import { RouterOutlet } from '@angular/router';

@Component({
    selector: 'layout-single',
    imports: [RouterOutlet],
    template: `
    <div class="page page-center">
      <div class="container container-tight py-4">
        <router-outlet></router-outlet>
      </div>
    </div>
  `,
    styles: [
        `
      :host {
        min-height: 100%;
        min-width: 100%;
      }
    `,
        `
      .page {
        background-image: url(https://bing.biturl.top/?resolution=1920&format=image&index=0&mkt=zh-CN), url('/assets/th.jpg'),
          linear-gradient(rgba(0, 0, 255, 0.5), rgba(255, 255, 0, 0.5));
        background-position: center;
        background-repeat: no-repeat;
        background-size: cover;
      }
    `,
    ]
})
export class SingleLayoutComponent implements OnInit {
  ngOnInit(): void {}
}

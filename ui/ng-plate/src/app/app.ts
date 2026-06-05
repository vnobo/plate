import { Component, ChangeDetectionStrategy } from '@angular/core';
import { RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet],
  template: `<router-outlet></router-outlet> `,
  styles: `
    :host {
      height: 100%;
      width: 100%;
      display: block;
    }

    .app-content {
      padding: 20px;
      text-align: center;
    }
  `,
})
export class App {}

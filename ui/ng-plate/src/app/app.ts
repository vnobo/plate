import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-root',
  standalone: true,
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

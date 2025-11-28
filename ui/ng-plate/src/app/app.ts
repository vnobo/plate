import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
@Component({
  selector: 'app-root',
  imports: [RouterOutlet],
  template: ` <router-outlet></router-outlet> `,
  styles: `
    :host {
      height: 100%;
      width: 100%;
    }
  `,
})
export class App {
  protected readonly title = signal('ng-plate');
}

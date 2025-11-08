import { ChangeDetectionStrategy, Component } from '@angular/core';
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
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class BlankLayout {}

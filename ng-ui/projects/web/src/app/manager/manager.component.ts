import { Component } from '@angular/core';
import { PageHeaderComponent } from '../../core/components/page-header/page-header.component';
import { PageContentComponent } from '../../core/components/page-content/page-content.component';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-manager',
  standalone: true,
  imports: [PageHeaderComponent, PageContentComponent, RouterModule],
  template: `
    <app-page-header></app-page-header>
    <app-page-content>
      <div class="row">
        <router-outlet>view content</router-outlet>
      </div>
    </app-page-content>
  `,
  styles: `
  :host {
    min-height: 100%;
    min-width: 100%;
  }
  `,
})
export class ManagerComponent {}

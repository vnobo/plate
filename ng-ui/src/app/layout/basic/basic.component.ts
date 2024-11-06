import { Component, signal } from '@angular/core';
import { LayoutSidebarComponent } from './widgets/sidebar.component';
import { LayoutHeaderComponent } from './widgets/header.component';
import { SharedModule } from '@app/shared/shared.module';

@Component({
  selector: 'layout-basic',
  standalone: true,
  imports: [SharedModule, LayoutHeaderComponent, LayoutSidebarComponent],
  template: `<nz-layout class="app-layout">
    <layout-basic-sidebar [isCollapsed]="isCollapsed()" ]></layout-basic-sidebar>
    <nz-layout>
      <layout-basic-header (onIsCollapsed)="this.onHeaderIsCollapsed($event)" )></layout-basic-header>
      <div class="container-fluid">
        <router-outlet></router-outlet>
      </div>
    </nz-layout>
  </nz-layout>`,
  styles: ``,
})
export class BasicLayoutComponent {
  isCollapsed = signal(false);

  onHeaderIsCollapsed = (isCollapsed: boolean) => {
    this.isCollapsed.set(isCollapsed);
  };
}

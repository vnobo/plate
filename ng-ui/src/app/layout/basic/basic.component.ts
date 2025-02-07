import { Component, signal } from '@angular/core';
import { SharedModule } from '@app/shared/shared.module';
import { LayoutNavbarComponent } from './widgets/navbar.component';
import { LayoutSidebarComponent } from './widgets/sidebar.component';

@Component({
  selector: 'layout-basic',
  imports: [SharedModule, LayoutNavbarComponent, LayoutSidebarComponent],
  template: `
    <nz-layout class="page">
      <nz-sider [nzCollapsed]="isCollapsed()" [nzTrigger]="null" class="menu-sidebar" nzBreakpoint="md" nzCollapsible nzWidth="14.8rem">
        <div layoutSidebarMenus></div>
      </nz-sider>
      <nz-layout class="page-wrapper">
        <nz-header class="nz-header">
          <div layoutNavbar (outputCollapsed)="onHeaderIsCollapsed($event)"></div>
        </nz-header>
        <nz-content class="container-fluid mt-2">
          <router-outlet></router-outlet>
        </nz-content>
      </nz-layout>
    </nz-layout>
  `,
  styles: [
    `
      :host {
        display: flex;
        text-rendering: optimizeLegibility;
        -webkit-font-smoothing: antialiased;
        -moz-osx-font-smoothing: grayscale;
      }
    `,
    `
      .menu-sidebar {
        position: relative;
        z-index: 10;
        min-height: 100vh;
        box-shadow: 2px 0 6px rgba(0, 21, 41, 0.35);
      }
    `,
    `
      .nz-header {
        padding: 0;
        height: auto;
        line-height: normal;
      }
    `,
  ],
})
export class BasicLayoutComponent {
  isCollapsed = signal(false);

  onHeaderIsCollapsed = (isCollapsed: boolean) => {
    this.isCollapsed.set(isCollapsed);
  };
}

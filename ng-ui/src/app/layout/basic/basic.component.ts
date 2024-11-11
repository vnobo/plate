import { Component, signal } from '@angular/core';
import { LayoutSidebarComponent } from './widgets/sidebar.component';
import { LayoutHeaderComponent } from './widgets/header.component';
import { SharedModule } from '@app/shared/shared.module';
import { NzPageHeaderModule } from 'ng-zorro-antd/page-header';
import { NzBreadCrumbModule } from 'ng-zorro-antd/breadcrumb';

@Component({
  selector: 'layout-basic',
  standalone: true,
  imports: [SharedModule, LayoutHeaderComponent, LayoutSidebarComponent, NzPageHeaderModule, NzBreadCrumbModule],
  template: `
    <nz-layout class="nz-page">
      <nz-sider
        [nzCollapsed]="isCollapsed()"
        [nzTrigger]="null"
        class="menu-sidebar"
        nzBreakpoint="md"
        nzCollapsible
        nzWidth="14.8rem">
        <layout-sidebar-menus></layout-sidebar-menus>
      </nz-sider>
      <nz-layout class="nz-page-wrapper">
        <nz-header>
          <layout-page-header (onIsCollapsed)="this.onHeaderIsCollapsed($event)"></layout-page-header>
        </nz-header>
        <nz-page-header nzBackIcon [nzGhost]="false">
          <nz-breadcrumb [nzAutoGenerate]="true"></nz-breadcrumb>
          <nz-page-header-title>Title</nz-page-header-title>
          <nz-page-header-subtitle>This is a subtitle</nz-page-header-subtitle>
          <nz-page-header-extra>
          </nz-page-header-extra>
          <nz-page-header-content>
          </nz-page-header-content>
        </nz-page-header>
        <div class="container-fluid">
          <router-outlet></router-outlet>
        </div>
      </nz-layout>
    </nz-layout>
  `,
  styles: [`
    :host {
      display: flex;
      text-rendering: optimizeLegibility;
      -webkit-font-smoothing: antialiased;
      -moz-osx-font-smoothing: grayscale;
    }`,
    `.nz-page {
      height: 100vh;
    }`,
    `.menu-sidebar {
      position: relative;
      z-index: 10;
      min-height: 100vh;
      box-shadow: 2px 0 6px rgba(0, 21, 41, .35);
    }
    `, `nz-header {
      padding: 0;
      width: 100%;
      z-index: 2;
    }`,
  ],
})
export class BasicLayoutComponent {
  isCollapsed = signal(false);

  onHeaderIsCollapsed = (isCollapsed: boolean) => {
    this.isCollapsed.set(isCollapsed);
  };
}

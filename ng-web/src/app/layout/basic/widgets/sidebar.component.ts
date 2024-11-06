import { Component, inject, input } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { RouterModule } from '@angular/router';
import { NzLayoutModule } from 'ng-zorro-antd/layout';
import { NzMenuModule } from 'ng-zorro-antd/menu';
import { MenusService } from '../../../pages';

@Component({
  selector: 'layout-basic-sidebar',
  standalone: true,
  imports: [NzLayoutModule, NzMenuModule, RouterModule],
  template: ` <nz-sider
    [nzCollapsed]="isCollapsed"
    [nzTrigger]="null"
    class="menu-sidebar"
    nzBreakpoint="md"
    nzCollapsible
    nzWidth="14.8rem">
    <div class="sidebar-logo">
      <a href="/" rel="noopener noreferrer" target="_blank">
        <img alt="logo" src="assets/img/logo.png" />
        <h1>Plate 管理后台</h1>
      </a>
    </div>
    <ul nz-menu nzMode="inline" nzTheme="dark">
      <li nz-submenu nzIcon="dashboard" nzOpen nzTitle="Dashboard">
        <ul>
          <li nz-menu-item nzMatchRouter>
            <a routerLink="/welcome">Welcome</a>
          </li>
          <li nz-menu-item nzMatchRouter>
            <a>Monitor</a>
          </li>
          <li nz-menu-item nzMatchRouter>
            <a>Workplace</a>
          </li>
        </ul>
      </li>
      @for (menu of myMenus(); track menu) {
      <li nz-submenu nzIcon="{{ menu.icons }}" nzOpen nzTitle="{{ menu.name }}">
        <ul>
          @for (children of menu.children; track children) {
          <li nz-menu-item nzMatchRouter>
            <a nz-button nzType="link" routerLink="{{ children.path }}">{{ children.name }}</a>
          </li>
          }
        </ul>
      </li>
      }
    </ul>
  </nz-sider>`,
  styles: `
  :host {
  display: flex;
  text-rendering: optimizeLegibility;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}
.menu-sidebar {
  position: relative;
  z-index: 10;
  min-height: 100vh;
  box-shadow: 2px 0 6px rgba(0, 21, 41, 0.35);
}

.header-trigger {
  height: 64px;
  padding: 20px 24px;
  font-size: 20px;
  cursor: pointer;
  transition: all 0.3s, padding 0s;
}

.trigger:hover {
  color: #1890ff;
}

.sidebar-logo {
  position: relative;
  height: 64px;
  padding-left: 24px;
  overflow: hidden;
  line-height: 64px;
  background: #001529;
  transition: all 0.3s;
}

.sidebar-logo img {
  display: inline-block;
  height: 32px;
  width: 32px;
  vertical-align: middle;
}

.sidebar-logo h1 {
  display: inline-block;
  margin: 0 0 0 20px;
  color: #fff;
  font-weight: 600;
  font-size: 14px;
  font-family: Avenir, Helvetica Neue, Arial, Helvetica, sans-serif;
  vertical-align: middle;
}
`,
})
export class LayoutSidebarComponent {
  private readonly menusSer = inject(MenusService);

  myMenus = toSignal(this.menusSer.getMyMenus({ pcode: '0', tenantCode: '0' }));
  isCollapsed = input.required<boolean>();
}

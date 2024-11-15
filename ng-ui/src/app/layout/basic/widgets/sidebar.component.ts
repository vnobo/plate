import { Component, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { RouterModule } from '@angular/router';
import { MenusService } from '@app/pages';
import { SharedModule } from '@app/shared/shared.module';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzLayoutModule } from 'ng-zorro-antd/layout';
import { NzMenuModule } from 'ng-zorro-antd/menu';

@Component({
  selector: 'layout-sidebar-menus, [layoutSidebarMenus]',
  standalone: true,
  imports: [NzLayoutModule, NzMenuModule, RouterModule, NzIconModule, SharedModule],
  template: `
    <div class="sidebar-logo">
      <a href="/" rel="noopener noreferrer" target="_blank">
        <img alt="logo" src="assets/img/logo.png" />
        <h1>Plate 管理后台</h1>
      </a>
    </div>
    <ul nz-menu nzMode="inline" nzTheme="dark">
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
    </ul>`,
  styles: [
    `
      :host {
        min-width: 100%;
        min-height: 100%;
      }
    `,
    `
      .sidebar-logo {
        position: relative;
        height: 64px;
        padding-left: 24px;
        overflow: hidden;
        line-height: 64px;
        transition: all 0.3s;
      }
    `,
    `
      .sidebar-logo img {
        display: inline-block;
        height: 32px;
        width: 32px;
        vertical-align: middle;
      }
    `,
    `
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
    `
      .header-trigger {
        height: 64px;
        padding: 20px 24px;
        font-size: 20px;
        cursor: pointer;
        transition: all 0.3s, padding 0s;
      }
    `,
    `
      .trigger:hover {
        color: #1890ff;
      }
    `,
  ],
})
export class LayoutSidebarComponent {
  private readonly menusSer = inject(MenusService);

  myMenus = toSignal(this.menusSer.getMyMenus({ pcode: '0', tenantCode: '0' }));
}

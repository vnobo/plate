import { Component } from '@angular/core';
import { RouterModule } from '@angular/router';
import { NzLayoutModule } from 'ng-zorro-antd/layout';
import { NzMenuModule } from 'ng-zorro-antd/menu';

@Component({
  selector: 'app-page-header',
  standalone: true,
  imports: [NzLayoutModule, NzMenuModule, RouterModule],
  template: `
    <nz-header class="nz-header d-flex justify-content-between align-content-between">
      <div class="logo"><i class="bi bi-p-circle-fill mx-2"></i>PLATE</div>
      <ul class="header-menu" nz-menu nzMode="horizontal" nzTheme="dark"></ul>
      <ul class="header-menu ms-auto" nz-menu nzMode="horizontal" nzTheme="dark">
        <li nz-menu-item>
          <a routerLink="/home" routerLinkActive="active">首&nbsp;&nbsp;&nbsp;&nbsp;页</a>
        </li>
        <li nz-menu-item>
          <a routerLink="/manager" routerLinkActive="active">管理平台</a>
        </li>
        <li nz-menu-item>
          <a routerLink="/profie" routerLinkActive="active">我&nbsp;&nbsp;&nbsp;&nbsp;的</a>
        </li>
      </ul>
    </nz-header>
  `,
  styles: `
  .host{
    min-width: 100%;
    min-height: 100%;
    display: block;
  }
  .nz-header {
    padding: 0 !important;
    color: white;
    a {
      font-size: 0.9rem;
    }
  }

.logo {
  width: 14rem;
  color: white;
  float: left;
  font-size: 1.4rem;
  justify-content: center;
  align-items: center;
  padding-left: 0.5rem;
}

.header-menu {
  line-height: 4rem;
  margin-right: 1rem;
}
  `,
})
export class PageHeaderComponent {}

import { Component } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzDropDownModule } from 'ng-zorro-antd/dropdown';
import { NzLayoutModule } from 'ng-zorro-antd/layout';
import { NzMenuModule } from 'ng-zorro-antd/menu';
import { LoginService } from '../login/login.service';
import { NzAvatarModule } from 'ng-zorro-antd/avatar';

@Component({
  selector: 'app-page-header',
  standalone: true,
  imports: [NzLayoutModule, NzMenuModule, RouterModule, NzDropDownModule, NzButtonModule, NzAvatarModule],
  template: `
    <nz-header class="nz-header d-flex justify-content-between align-content-between">
      <div class="logo"><i class="bi bi-p-circle-fill mx-2"></i>PLATE</div>
      <ul class="header-menu" nz-menu nzMode="horizontal" nzTheme="dark"></ul>
      <ul class="header-menu ms-auto" nz-menu nzMode="horizontal" nzTheme="dark">
        <li nz-menu-item>
          <a routerLink="/home" routerLinkActive="active">首页</a>
        </li>
        <li nz-menu-item>
          <a routerLink="/manager" routerLinkActive="active">管理平台</a>
        </li>
        <li nz-menu-item nz-dropdown nzTrigger="click" [nzDropdownMenu]="menu" [nzPlacement]="'bottomRight'">
          <nz-avatar nzIcon="user" style="color:#f56a00; background-color:#fde3cf;"></nz-avatar>
          <a class="ms-1 pt-2" href="javascript:void(0);">我的<span nz-icon nzType="down"></span></a>
        </li>
        <nz-dropdown-menu #menu="nzDropdownMenu">
          <ul nz-menu>
            <li nz-menu-item>
              <a nz-button nzType="link" nzDanger (click)="loginOut()">账号退出</a>
            </li>
          </ul>
        </nz-dropdown-menu>
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
export class PageHeaderComponent {
  constructor(private _loginSer: LoginService, private _route: Router) {}

  loginOut() {
    this._loginSer.logout().subscribe(res => this._route.navigate([this._loginSer._auth.loginUrl]).then());
  }
}

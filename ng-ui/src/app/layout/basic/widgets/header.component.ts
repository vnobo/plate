import { Component, inject, output, signal } from '@angular/core';
import { LoginService } from '@app/pages';
import { ActivatedRoute, Router } from '@angular/router';
import { SHARED_IMPORTS } from '@app/shared/shared-imports';

@Component({
  selector: 'layout-page-header',
  standalone: true,
  imports: [SHARED_IMPORTS],
  template: `
    <div class="nz-page-header">
      <span (click)="this.setNewName(!isCollapsed())" class="header-trigger">
        <span [nzType]="isCollapsed() ? 'menu-unfold' : 'menu-fold'" class="trigger" nz-icon></span>
      </span>
    </div>
    <div class="ms-auto">
      <ul nz-menu nzMode="horizontal" nzTheme="dark">
        <li nz-menu-item>
          <span nz-icon nzType="home"></span>
          <a class="p-1" href="/home" nz-button nzType="link">首页</a>
        </li>
        <li nzPlacement="bottomRight" nz-submenu nzIcon="user" nzTitle="我的">
          <ul>
            <li nz-menu-item>
              <a (click)="loginOut()" nz-button nzDanger nzType="link">账号退出</a>
            </li>
          </ul>
        </li>
      </ul>
    </div>`,
  styles: [
    `:host {
      display: flex;
      flex-direction: row;
      justify-content: space-between;
      min-height: 100%;
      min-width: 100%;
    }`,
    `.nz-page-header {
      position: relative;
      padding: 0;
      box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
      height: 100%;
    }`,
    `.header-trigger {
      height: 64px;
      padding: 20px 24px;
      font-size: 20px;
      cursor: pointer;
      transition: all .3s, padding 0s;
    }`,
  ],
})
export class LayoutHeaderComponent {
  isCollapsed = signal(false);
  onIsCollapsed = output<boolean>();
  private readonly loginSer = inject(LoginService);

  constructor(private readonly router: Router, private readonly route: ActivatedRoute) {
  }

  setNewName(isCollapsed: boolean) {
    this.isCollapsed.set(isCollapsed);
    this.onIsCollapsed.emit(isCollapsed);
  }

  loginOut() {
    this.loginSer.logout();
    this.router.navigate([this.loginSer._auth.loginUrl], { relativeTo: this.route }).then();
  }
}

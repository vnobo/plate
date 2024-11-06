import { Component, inject, output, signal } from '@angular/core';
import { SharedModule } from '../../../shared/shared.module';
import { LoginService } from '../../../pages';
import { ActivatedRoute, Router } from '@angular/router';

@Component({
  selector: 'layout-basic-header',
  standalone: true,
  imports: [SharedModule],
  template: ` <nz-header class="hstack gap-2">
    <div class="app-header">
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
        <li [nzPlacement]="'bottomRight'" nz-submenu nzIcon="user" nzTitle="我的">
          <ul>
            <li nz-menu-item>
              <a (click)="loginOut()" nz-button nzDanger nzType="link">账号退出</a>
            </li>
          </ul>
        </li>
      </ul>
    </div>
  </nz-header>`,
  styles: `nz-header {
  padding: 0;
  width: 100%;
  z-index: 2;
}

.app-header {
  position: relative;
  padding: 0;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  height: 100%;
}
.app-header-right {
  position: relative;
  height: 100%;
  padding: 0;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
}

.header-menu {
  line-height: 4rem;
}`,
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

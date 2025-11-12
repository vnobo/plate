import { Component, inject, signal, afterNextRender } from '@angular/core';
import { RouterModule } from '@angular/router';
import { TokenService } from '@app/core';
import { UserDetails } from '@plate/types';

@Component({
  selector: 'app-aside-menu',
  standalone: true,
  imports: [RouterModule],
  template: `
    <div class="sidebar sidebar-main">
      <div class="sidebar-content">
        <div class="sidebar-user">
          <div class="d-flex">
            <div class="flex-shrink-0">
              <span class="avatar avatar-sm" [style]="userAvatar()"></span>
            </div>
            <div class="flex-grow-1 ms-3">
              <div class="font-semibold">{{ userDetails().name }}</div>
              <div class="text-xs text-secondary">{{ userDetails().nickname }}</div>
            </div>
          </div>
        </div>
        <div class="mt-5">
          <ul class="nav nav-main flex-column">
            <li class="nav-item">
              <a href="#" class="nav-link">
                <i class="ph ph-house"></i>
                <span>首页</span>
              </a>
            </li>
            <li class="nav-item">
              <a href="#" class="nav-link">
                <i class="ph ph-package-open"></i>
                <span>产品管理</span>
              </a>
            </li>
            <li class="nav-item">
              <a href="#" class="nav-link">
                <i class="ph ph-shopping-cart"></i>
                <span>订单管理</span>
              </a>
            </li>
            <li class="nav-item">
              <a href="#" class="nav-link">
                <i class="ph ph-users"></i>
                <span>客户管理</span>
              </a>
            </li>
            <li class="nav-item">
              <a href="#" class="nav-link">
                <i class="ph ph-chart-line"></i>
                <span>数据分析</span>
              </a>
            </li>
            <li class="nav-item">
              <a href="#" class="nav-link">
                <i class="ph ph-cog"></i>
                <span>系统设置</span>
              </a>
            </li>
          </ul>
        </div>
      </div>
    </div>
  `,
  styles: [
    `
      .avatar {
        background-image: url('/assets/img/avater.png');
      }
    `,
  ],
})
export class AsideMenuComponent {
  private readonly _tokenSer = inject(TokenService);

  userDetails = signal({} as UserDetails);
  userAvatar = signal({
    backgroundImage: "url('assets/img/avater.png')",
  });

  constructor() {
    afterNextRender(() => {
      this._tokenSer.isLoggedIn$.subscribe((isLoggedIn) => {
        if (isLoggedIn) {
          const authentication = this._tokenSer.authenticationToken();
          if (authentication) {
            this.userDetails.set(authentication.details);
            if (authentication.details.avatar) {
              this.userAvatar.set({
                backgroundImage: 'url(' + authentication.details.avatar + ')',
              });
            }
          }
        }
      });
    });
  }
}

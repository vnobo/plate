import { afterNextRender, Component, inject, OnInit, signal } from '@angular/core';
import { RouterModule } from '@angular/router';
import { TokenService } from '@app/core';
import { UserDetails } from '@plate/types';
import { AsideMenuComponent } from './aside-menu';
import { LayoutHeader } from './layout-header';

@Component({
  selector: 'app-layout-base',
  imports: [RouterModule, AsideMenuComponent, LayoutHeader],
  template: `
    <div class="page">
      <app-layout-header></app-layout-header>
      <div class="page">
        <app-aside-menu></app-aside-menu>
        <div class="page-wrapper">
          <div class="container-xxl">
            <router-outlet></router-outlet>
          </div>
        </div>
      </div>
    </div>
  `,
})
export class AppLayoutBaseComponent implements OnInit {
  private tokenService = inject(TokenService);
  userDetails = signal<UserDetails | null>(null);
  userAvatar = signal<string | null>(null);

  ngOnInit() {
    afterNextRender(() => {
      this.tokenService.isLoggedIn$.subscribe((isLoggedIn) => {
        if (isLoggedIn) {
          const authentication = this.tokenService.authenticationToken();
          if (authentication) {
            this.userDetails.set(authentication.details);
            if (authentication.details.avatar) {
              this.userAvatar.set(authentication.details.avatar);
            }
          }
        }
      });
    });
  }
}

import { CommonModule } from '@angular/common';
import { Component, inject, type OnInit } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { ActivatedRoute, RouterLink, RouterOutlet } from '@angular/router';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzLayoutModule } from 'ng-zorro-antd/layout';
import { NzMenuModule } from 'ng-zorro-antd/menu';
import { NzBreadCrumbModule } from 'ng-zorro-antd/breadcrumb';
import { MenusService } from './menus/menus.service';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzDropDownModule } from 'ng-zorro-antd/dropdown';
import { NzAvatarModule } from 'ng-zorro-antd/avatar';
import { NzPageHeaderModule } from 'ng-zorro-antd/page-header';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    RouterOutlet,
    NzIconModule,
    NzLayoutModule,
    NzMenuModule,
    NzButtonModule,
    NzBreadCrumbModule,
    NzDropDownModule,
    NzAvatarModule,
    NzPageHeaderModule,
  ],
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss',
})
export class HomeComponent implements OnInit {
  _menusSer = inject(MenusService);
  _activatedRoute = inject(ActivatedRoute);

  myMenus = toSignal(this._menusSer.getMyMenus({ pcode: '0', tenantCode: '0' }));
  isCollapsed = false;

  ngOnInit(): void {
  }

  loginOut() {
  }
}

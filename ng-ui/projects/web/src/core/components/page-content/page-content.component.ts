import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit, signal, Type } from '@angular/core';
import { ActivatedRoute, Params, PRIMARY_OUTLET, Resolve, ResolveFn, RouterModule } from '@angular/router';
import { NzBreadCrumbModule } from 'ng-zorro-antd/breadcrumb';
import { NzLayoutModule } from 'ng-zorro-antd/layout';
import { NzMenuModule } from 'ng-zorro-antd/menu';
import { Subject, takeUntil } from 'rxjs';
import { MenusService } from '../../../app/manager/system/menus/menus.service';
import { Menu } from '../../../app/manager/system/menus/menu.types';

import { IconDefinition } from '@ant-design/icons-angular';
import { NzIconModule, NzIconService } from 'ng-zorro-antd/icon';
import * as AllIcons from '@ant-design/icons-angular/icons';

const antDesignIcons = AllIcons as {
  [key: string]: IconDefinition;
};
const iconsAll: IconDefinition[] = Object.keys(antDesignIcons).map(key => antDesignIcons[key]);

export interface Breadcrumb {
  label: string | ResolveFn<string> | Type<Resolve<string>>;
  params: Params;
  url: string;
}

@Component({
  selector: 'app-page-content',
  standalone: true,
  imports: [NzLayoutModule, NzMenuModule, CommonModule, RouterModule, NzBreadCrumbModule, NzIconModule],
  template: `
    <nz-layout class="content-layout">
      <nz-sider [nzCollapsedWidth]="0" nzBreakpoint="md" nzCollapsible nzWidth="14.6rem">
        <ul nz-menu class="sider-menu" nzMode="inline" nzTheme="dark">
          <li nz-menu-item>
            <a routerLink="/home"
              ><span nz-icon nzType="home" nzTheme="twotone" class="me-2" style="font-size: 1rem;"></span>首页</a
            >
          </li>
          @for (menu of menus(); track menu) {
          <li nz-submenu nzOpen [nzTitle]="titleTpl">
            <ng-template #titleTpl>
              <span title
                ><span nz-icon nzType="appstore" nzTheme="twotone" style="font-size: 1rem;"></span
                ><span>{{ menu.name }}</span></span
              ></ng-template
            >
            <ul>
              @for (child of menu.children; track child) {
              <li nz-menu-item>
                @if (child.icons) {
                <i class="me-1" [class]="child.icons" style="font-size: 1rem; color: cornflowerblue;"></i>
                }
                <a [state]="child" routerLink="{{ child.path }}" routerLinkActive="active">{{ child.name }}</a>
              </li>
              }
            </ul>
          </li>
          }
        </ul>
      </nz-sider>
      <nz-layout class="inner-layout">
        <nz-breadcrumb>
          @for (breadcrumb of breadcrumbs(); track breadcrumb) {
          <nz-breadcrumb-item
            ><a routerLink="{{ breadcrumb.url }}">{{ breadcrumb.label }}</a></nz-breadcrumb-item
          >
          }
        </nz-breadcrumb>
        <nz-content>
          <div class="inner-content">
            <ng-component></ng-component>
          </div>
        </nz-content>
        <nz-footer>Ant Design ©2024 Implement By Angular</nz-footer>
      </nz-layout>
    </nz-layout>
  `,
  styles: `
  :host {
    min-height: 100%;
  }
  .content-layout{
    min-height: 91vh;
  }
  .sider-menu {
  min-height: 100%;
}

.inner-layout {
  padding: 0 1.2rem 1.2rem;
}

nz-breadcrumb {
  margin: 1rem 0;
}

nz-content {
  background: #fff;
  padding: 1rem;
}`,
})
export class PageContentComponent implements OnInit, OnDestroy {
  menus = signal([] as Menu[]);
  breadcrumbs = signal([] as Breadcrumb[]);

  private _subject: Subject<void> = new Subject<void>();

  constructor(
    private activatedRoute: ActivatedRoute,
    private iconService: NzIconService,
    private menusService: MenusService
  ) {
    for (const icon of iconsAll) {
      this.iconService.addIcon(icon);
    }
  }

  ngOnDestroy(): void {
    this._subject.next();
    this._subject.complete();
  }

  ngOnInit() {
    this.initMenu();
    this.breadcrumbs.set(this.initBreadcrumbs(this.activatedRoute.root));
  }

  initMenu() {
    const menuRequest: Menu = {
      pcode: '0',
      tenantCode: '0',
    };
    this.menusService
      .getMeMenus(menuRequest)
      .pipe(takeUntil(this._subject))
      .subscribe(menus => this.menus.set(menus));
  }

  private initBreadcrumbs(route: ActivatedRoute, url = '', breads: Breadcrumb[] = []): Breadcrumb[] {
    const children: ActivatedRoute[] = route.children;
    if (children.length === 0) {
      return breads;
    }
    for (const child of children) {
      // verify primary route
      if (child.outlet !== PRIMARY_OUTLET) {
        continue;
      }
      if (!child.snapshot.url.length) {
        return this.initBreadcrumbs(child, url, breads);
      }
      const title = child.snapshot.routeConfig?.title;
      const routeURL: string = child.snapshot.url.map(segment => segment.path).join('/');

      url += `/${routeURL}`;
      const breadcrumb: Breadcrumb = {
        label: title ? title : 'title',
        params: child.snapshot.params,
        url,
      };
      breads.push(breadcrumb);
      return this.initBreadcrumbs(child, url, breads);
    }
    return breads;
  }
}

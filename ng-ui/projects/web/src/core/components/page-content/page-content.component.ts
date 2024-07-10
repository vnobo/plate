import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit, signal, Type } from '@angular/core';
import {
  ActivatedRoute,
  NavigationEnd,
  Params,
  PRIMARY_OUTLET,
  Resolve,
  ResolveFn,
  Router,
  RouterModule,
} from '@angular/router';
import { NzBreadCrumbModule } from 'ng-zorro-antd/breadcrumb';
import { NzLayoutModule } from 'ng-zorro-antd/layout';
import { NzMenuModule } from 'ng-zorro-antd/menu';
import { distinctUntilChanged, filter, map, Subject, takeUntil } from 'rxjs';
import { MenusService } from '../../../app/manager/system/menus/menus.service';
import { Menu } from '../../../app/manager/system/menus/menu.types';

export interface Breadcrumb {
  label: string | ResolveFn<string> | Type<Resolve<string>>;
  params: Params;
  url: string;
}

@Component({
  selector: 'app-page-content',
  standalone: true,
  imports: [NzLayoutModule, NzMenuModule, CommonModule, RouterModule, NzBreadCrumbModule],
  template: `
    <nz-layout class="layout">
      <nz-sider [nzCollapsedWidth]="0" nzBreakpoint="md" nzCollapsible nzWidth="14.6rem">
        <ul nz-menu class="sider-menu" nzMode="inline" nzTheme="dark">
          <li nz-menu-item><i class="bi bi-p-circle-fill mx-2"></i><a routerLink="/home">首页</a></li>
          @for (menu of menus(); track menu) {
          <li nz-submenu nzIcon="user" nzOpen nzTitle="{{ menu.name }}">
            <ul>
              @for (child of menu.children; track child) {
              <li nz-menu-item>
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
  .layout{
    min-height: 91vh;
  }
  .sider-menu {
  min-height: 100%;
  border-right: 0;
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

  constructor(private router: Router, private activatedRoute: ActivatedRoute, private menusService: MenusService) {}

  ngOnDestroy(): void {
    this._subject.next();
    this._subject.complete();
  }

  ngOnInit() {
    this.initMenu();
    this.initBreadcrumb();
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

  initBreadcrumb() {
    this.breadcrumbs.set(this.getBreadcrumbs(this.activatedRoute.root));
    this.router.events
      .pipe(
        filter(event => event instanceof NavigationEnd),
        distinctUntilChanged(),
        map(() => this.getBreadcrumbs(this.activatedRoute.root)),
        takeUntil(this._subject)
      )
      .subscribe(event => this.breadcrumbs.set(event));
  }

  getBreadcrumbs(route: ActivatedRoute, url = '', breads: Breadcrumb[] = []): Breadcrumb[] {
    const children: ActivatedRoute[] = route.children;
    if (children.length === 0) {
      return breads;
    }
    for (const child of children) {
      // verify primary route
      if (child.outlet !== PRIMARY_OUTLET) {
        continue;
      }
      if (!child.snapshot.routeConfig?.title || !child.snapshot.url.length) {
        return this.getBreadcrumbs(child, url, breads);
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
      return this.getBreadcrumbs(child, url, breads);
    }
    return breads;
  }
}

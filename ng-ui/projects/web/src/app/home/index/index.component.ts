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
import { distinctUntilChanged, filter, map, Subject, takeUntil } from 'rxjs';
import { Menu } from '../menus/menu.types';
import { MenusService } from '../menus/menus.service';
import { CommonModule } from '@angular/common';
import { NzMenuModule } from 'ng-zorro-antd/menu';

export interface Breadcrumb {
  label: string | ResolveFn<string> | Type<Resolve<string>>;
  params: Params;
  url: string;
}

@Component({
  selector: 'home-index',
  standalone: true,
  imports: [NzLayoutModule, NzBreadCrumbModule, RouterModule, CommonModule, NzMenuModule],
  templateUrl: './index.component.html',
  styleUrl: './index.component.scss',
})
export class IndexComponent implements OnInit, OnDestroy {
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
      .subscribe(
        menus => this.menus.set(menus),
        err => console.log(err)
      );
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

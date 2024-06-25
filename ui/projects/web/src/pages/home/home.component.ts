import {Component, OnInit, Type} from '@angular/core';
import {distinctUntilChanged, filter, map, Observable} from 'rxjs';
import {Menu, MenusService} from '../system/menus/menus.service';
import {ActivatedRoute, NavigationEnd, Params, PRIMARY_OUTLET, Resolve, ResolveFn, Router,} from '@angular/router';

export interface Breadcrumb {
  label: string | ResolveFn<string> | Type<Resolve<string>>;
  params: Params;
  url: string;
}

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss'],
})
export class HomeComponent implements OnInit {
  menus$: Observable<Menu[]> | undefined;
  breadcrumbs: Breadcrumb[] = [];

  constructor(
    private router: Router,
    private activatedRoute: ActivatedRoute,
    private menusService: MenusService
  ) {
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
    this.menus$ = this.menusService.getMeMenus(menuRequest);
  }

  initBreadcrumb() {
    this.breadcrumbs = this.getBreadcrumbs(this.activatedRoute.root);
    this.router.events
      .pipe(
        filter(event => event instanceof NavigationEnd),
        distinctUntilChanged(),
        map(() => this.getBreadcrumbs(this.activatedRoute.root))
      )
      .subscribe(event => (this.breadcrumbs = event));
  }

  getBreadcrumbs(
    route: ActivatedRoute,
    url = '',
    breads: Array<Breadcrumb> = []
  ): Breadcrumb[] {
    const children: Array<ActivatedRoute> = route.children;
    if (children.length === 0) {
      return breads;
    }
    for (const child of children) {
      // verify primary route
      if (child.outlet !== PRIMARY_OUTLET) {
        continue;
      }
      if (
        !child.snapshot.routeConfig?.title ||
        !child.snapshot.url.length
      ) {
        return this.getBreadcrumbs(child, url, breads);
      }
      const title = child.snapshot.routeConfig?.title;
      const routeURL: string = child.snapshot.url
        .map(segment => segment.path)
        .join('/');

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

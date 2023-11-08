import {Component, OnInit} from '@angular/core';
import {distinctUntilChanged, filter, map, Observable} from "rxjs";
import {Menu} from "../../core/interfaces/menu";
import {MenusService} from "../../core/menus.service";
import {ActivatedRoute, NavigationEnd, Params, PRIMARY_OUTLET, Router} from "@angular/router";

export interface Breadcrumb {
  label: string;
  params: Params;
  url: string;
}

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss']
})
export class HomeComponent implements OnInit {

  menus$: Observable<Menu[]> | undefined;
  breadcrumbs: Breadcrumb[] = [];
  breadcrumbData: string | undefined;

  constructor(private router: Router,
              private activatedRoute: ActivatedRoute,
              private menusService: MenusService) {
  }

  ngOnInit() {
    this.initMenu();
    this.initBreadcrumb();
  }

  initMenu() {
    const menuRequest: Menu = {
      pcode: "0",
      tenantCode: "0"
    };
    this.menus$ = this.menusService.getMenus(menuRequest);
  }

  initBreadcrumb() {
    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd),
      distinctUntilChanged(),
      map(() => this.getBreadcrumbs(this.activatedRoute.root))
    ).subscribe(event => {
      this.breadcrumbs = event;
      console.log(this.breadcrumbs)
    });
  }

  getBreadcrumbs(route: ActivatedRoute, url = '', breadcrumbs: Array<Breadcrumb> = []): Breadcrumb[] {
    const routeDataBreadcrumbs = 'title';
    const children: Array<ActivatedRoute> = route.children;
    if (children.length === 0) {
      return breadcrumbs;
    }
    for (const child of children) {
      // verify primary route
      if (child.outlet !== PRIMARY_OUTLET) {
        continue;
      }
      const routeData = child.snapshot.data;
      const hasBreadcrumb = !Object.prototype.hasOwnProperty.call(routeData, routeDataBreadcrumbs);
      if (!child.snapshot.url.length || hasBreadcrumb) {
        return this.getBreadcrumbs(child, url, breadcrumbs);
      }
      const routeURL: string = child.snapshot.url.map(segment => segment.path).join('/');

      url += `/${routeURL}`;

      const breadcrumb: Breadcrumb = {
        label: child.snapshot.data[routeDataBreadcrumbs] || this.breadcrumbData,
        params: child.snapshot.params,
        url
      };
      this.breadcrumbs.push(breadcrumb);

      return this.getBreadcrumbs(child, url, breadcrumbs);
    }
    return breadcrumbs;
  }

}

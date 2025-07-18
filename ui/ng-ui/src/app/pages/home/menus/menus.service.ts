import {HttpClient, HttpParams} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {concatMap, delay, from, map, mergeMap, Observable, retry, switchMap, toArray} from 'rxjs';
import {Menu} from './menu.types';
import {defaultPageable, Page, Pageable} from '@app/core/types';
import {environment} from '@environment/environment';

@Injectable({
  providedIn: 'root',
})
export class MenusService {
  private readonly API_PREFIX = environment.relaApiPath;

  constructor(private http: HttpClient) {}

  search(request: Menu, page: Pageable): Observable<Menu[]> {
    let params = new HttpParams({ fromObject: request as never });
    params = params.appendAll({ page: page.page - 1, size: page.size });
    for (const sort in page.sorts) {
      params = params.appendAll({ sort: page.sorts[sort] });
    }
    return this.http.get<Menu[]>(this.API_PREFIX + '/menus/search', { params: params });
  }

  page(request: Menu, page: Pageable): Observable<Page<Menu>> {
    let params = new HttpParams({ fromObject: request as never });
    params = params.appendAll({ page: page.page - 1, size: page.size });
    for (const sort in page.sorts) {
      params = params.appendAll({ sort: page.sorts[sort] });
    }
    return this.http.get<Page<Menu>>(this.API_PREFIX + '/menus/page', { params: params }).pipe(
      switchMap(page =>
        from(page.content).pipe(
          delay(100),
          mergeMap(item => {
            return this.search({ pcode: item.code }, defaultPageable).pipe(
              map(children => {
                if (children.length > 0) {
                  item.children = children;
                }
                return item;
              }),
            );
          }),
          toArray(),
          map(items => {
            page.content = items;
            return page;
          }),
        ),
      ),
    );
  }

  getMenus(request: Menu): Observable<Menu[]> {
    const params = new HttpParams({ fromObject: request as never });
    return this.http.get<Menu[]>(this.API_PREFIX + '/menus/search', { params: params }).pipe(
      concatMap((items: Menu[]) => {
        return from(items).pipe(
          delay(100),
          mergeMap(item => {
            return this.getMenus({ pcode: item.code }).pipe(
              map(children => {
                if (children.length > 0) {
                  item.children = children;
                }
                return item;
              }),
            );
          }),
        );
      }),
      toArray(),
    );
  }

  delete(menu: Menu): Observable<void> {
    return this.http.delete<void>(this.API_PREFIX + '/menus/delete', { body: menu });
  }

  save(menu: Menu): Observable<Menu> {
    return this.http.post<Menu>(this.API_PREFIX + '/menus/save', menu);
  }

  meMerge(request: Menu): Observable<Menu[]> {
    return this.myMenus(request).pipe(concatMap(this.childrenMap), toArray(), retry(3));
  }

  childrenMap = (items: Menu[]) => {
    return from(items).pipe(
      delay(100),
      mergeMap(item =>
        this.myMenus({ pcode: item.code }).pipe(
          map(children => {
            if (children.length > 0) {
              item.children = children;
            }
            return item;
          }),
        ),
      ),
    );
  };

  myMenus(request: Menu): Observable<Menu[]> {
    const params = new HttpParams({ fromObject: request as never });
    return this.http.get<Menu[]>(this.API_PREFIX + '/menus/me', { params: params });
  }
}

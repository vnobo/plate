import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { concatMap, delay, from, map, mergeMap, Observable, retry, switchMap, toArray } from 'rxjs';
import { Menu } from './menu.types';
import { defaultPageable, Page, Pageable } from '@app/core/types';

@Injectable({
  providedIn: 'root',
})
export class MenusService {
  constructor(private http: HttpClient) {
  }

  search(request: Menu, page: Pageable): Observable<Menu[]> {
    let params = new HttpParams({ fromObject: request as never });
    params = params.appendAll({ page: page.page - 1, size: page.size });
    for (const sort in page.sorts) {
      params = params.appendAll({ sort: page.sorts[sort] });
    }
    return this.http.get<Menu[]>('/menus/search', { params: params });
  }

  page(request: Menu, page: Pageable): Observable<Page<Menu>> {
    let params = new HttpParams({ fromObject: request as never });
    params = params.appendAll({ page: page.page - 1, size: page.size });
    for (const sort in page.sorts) {
      params = params.appendAll({ sort: page.sorts[sort] });
    }
    return this.http.get<Page<Menu>>('/menus/page', { params: params }).pipe(
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
    return this.http.get<Menu[]>('/menus/search', { params: params }).pipe(
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
    return this.http.delete<void>('/menus/delete', { body: menu });
  }

  save(menu: Menu): Observable<Menu> {
    return this.http.post<Menu>('/menus/save', menu);
  }

  getMyMenus(request: Menu): Observable<Menu[]> {
    const params = new HttpParams({ fromObject: request as never });
    return this.http.get<Menu[]>('/menus/me', { params: params }).pipe(concatMap(this.childrenMap), toArray(), retry(3));
  }

  childrenMap = (items: Menu[]) => {
    return from(items).pipe(
      delay(100),
      mergeMap(item =>
        this.getMyChildren({ pcode: item.code }).pipe(
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

  getMyChildren(request: Menu): Observable<Menu[]> {
    const params = new HttpParams({ fromObject: request as never });
    return this.http.get<Menu[]>('/menus/me', { params: params });
  }
}

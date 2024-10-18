import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { concatMap, delay, from, map, mergeMap, MonoTypeOperatorFunction, Observable, retry, toArray } from 'rxjs';
import { Menu } from './menu.types';

@Injectable({
  providedIn: 'root',
})
export class MenusService {
  pipe(arg0: MonoTypeOperatorFunction<unknown>) {
    throw new Error('Method not implemented.');
  }

  constructor(private http: HttpClient) {
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
          retry(3),
        );
      }),
      toArray(),
      retry(3),
    );
  }

  deleteMenu(menu: Menu): Observable<void> {
    return this.http.delete<void>('/menus/delete', { body: menu });
  }

  saveMenu(menu: Menu): Observable<Menu> {
    return this.http.post<Menu>('/menus/save', menu);
  }

  getMyMenus(request: Menu): Observable<Menu[]> {
    const params = new HttpParams({ fromObject: request as never });
    return this.http
      .get<Menu[]>('/menus/me', { params: params })
      .pipe(concatMap(this.childrenMap), toArray(), retry(3));
  }

  childrenMap = (items: Menu[]) => {
    return from(items).pipe(
      delay(100),
      mergeMap(item => {
        return this.getMyChildren({ pcode: item.code }).pipe(
          map(children => {
            if (children.length > 0) {
              item.children = children;
            }
            return item;
          }),
        );
      }),
      retry(3),
    );
  };

  getMyChildren(request: Menu): Observable<Menu[]> {
    const params = new HttpParams({ fromObject: request as never });
    return this.http.get<Menu[]>('/menus/me', { params: params });
  }
}

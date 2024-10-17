import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { concatMap, delay, from, map, mergeMap, Observable, retry, toArray } from 'rxjs';
import { Menu } from './menu.types';

@Injectable({
  providedIn: 'root',
})
export class MenusService {
  constructor(private http: HttpClient) {
  }

  childrenMap = (items: Menu[]) => {
    return from(items).pipe(
      delay(100),
      mergeMap(item => {
        return this.getChildren({ pcode: item.code }).pipe(
          map(children => {
            item.children = children;
            return item;
          })
        );
      }),
      retry(3),
    );
  };

  getMenus(request: Menu): Observable<Menu[]> {
    const params = new HttpParams({ fromObject: request as never });
    return this.http
      .get<Menu[]>('/menus/search', { params: params })
      .pipe(concatMap(this.childrenMap), toArray(), retry(3));
  }

  getMyMenus(request: Menu): Observable<Menu[]> {
    const params = new HttpParams({ fromObject: request as never });
    return this.http
      .get<Menu[]>('/menus/me', { params: params })
      .pipe(concatMap(this.childrenMap), toArray(), retry(3));
  }

  saveMenu(menu: Menu): Observable<Menu> {
    return this.http.post<Menu>('/menus/save', menu);
  }

  getChildren(request: Menu): Observable<Menu[]> {
    const params = new HttpParams({ fromObject: request as never });
    return this.http.get<Menu[]>('/menus/me', { params: params });
  }
}

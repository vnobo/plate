import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { concatMap, delay, from, map, mergeMap, Observable, retry, toArray } from 'rxjs';
import { Menu } from './menu.types';

@Injectable({
  providedIn: 'root',
})
export class MenusService {
  constructor(private http: HttpClient) {}

  pipe(arg0: any) {
    throw new Error('Method not implemented.');
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
      retry(3)
    );
  };

  getMenus(request: Menu): Observable<Menu[]> {
    const params = new HttpParams({ fromObject: request as never });
    return this.http
      .get<Menu[]>('/menus/search', { params: params })
      .pipe(concatMap(this.childrenMap), toArray(), retry(3));
  }

  getMeMenus(request: Menu): Observable<Menu[]> {
    const params = new HttpParams({ fromObject: request as never });
    return this.http
      .get<Menu[]>('/menus/me', { params: params })
      .pipe(concatMap(this.childrenMap), toArray(), retry(3));
  }

  getChildren(request: Menu): Observable<Menu[]> {
    const params = new HttpParams({ fromObject: request as never });
    return this.http.get<Menu[]>('/menus/me', { params: params });
  }
}

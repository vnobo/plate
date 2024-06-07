import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from "@angular/common/http";
import {concatMap, delay, from, map, mergeMap, Observable, retry, toArray} from "rxjs";

export interface Menu {
  id?: number;
  code?: string;
  pcode?: string;
  tenantCode?: string;
  type?: MenuType;
  authority?: string;
  name?: string;
  path?: string;
  sort?: number;
  extend?: never;
  creator?: UserAuditor;
  updater?: UserAuditor;
  createdTime?: Date;
  updatedTime?: Date;
  permissions?: Permission[];
  icons?: string;
  children?: Menu[];
  level?: number;
  expand?: boolean;
  parent?: Menu;
}

export interface UserAuditor {
  code: string;
  username: string;
  name?: string;
}

export enum MenuType {
  FOLDER = "FOLDER",
  MENU = "MENU",
  LINK = 'LINK',
  API = 'API'
}

export enum HttpMethod {
  GET = "GET",
  POST = "POST",
  PUT = "PUT",
  DELETE = "DELETE",
  ALL = "ALL",
}

export interface Permission {
  method: HttpMethod;
  name: string;
  path: string;
  authority: string;
}

@Injectable({
  providedIn: 'root'
})
export class MenusService {

  constructor(private http: HttpClient) {
  }

  childrenMap = (items: Menu[]) => {
    return from(items).pipe(delay(100),
      mergeMap(item => {
        return this.getChildren({pcode: item.code}).pipe(map(children => {
          item.children = children;
          return item;
        }));
      }), retry(3));
  }

  getMenus(request: Menu): Observable<Menu[]> {
    const params = new HttpParams({fromObject: request as never});
    return this.http.get<Menu[]>('/menus/search', {params: params})
      .pipe(concatMap(this.childrenMap), toArray(), retry(3));
  }

  getMeMenus(request: Menu): Observable<Menu[]> {
    const params = new HttpParams({fromObject: request as never});
    return this.http.get<Menu[]>('/menus/me', {params: params})
      .pipe(concatMap(this.childrenMap), toArray(), retry(3));
  }

  getChildren(request: Menu): Observable<Menu[]> {
    const params = new HttpParams({fromObject: request as never});
    return this.http.get<Menu[]>('/menus/me', {params: params});
  }


}

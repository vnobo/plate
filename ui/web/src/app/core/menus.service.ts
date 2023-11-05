import {Injectable} from '@angular/core';
import {HttpClient, HttpParams} from "@angular/common/http";
import {delay, from, map, mergeMap, Observable, retry, switchMap, toArray} from "rxjs";
import {Menu} from "./interfaces/menu";

@Injectable({
    providedIn: 'root'
})
export class MenusService {

    constructor(private http: HttpClient) {
    }

    getMenus(request: Menu): Observable<Menu[]> {
        let params = new HttpParams({fromObject: request as any});
        return this.http.get<Menu[]>('/menus/me', {params: params})
            .pipe(switchMap(items => {
                return from(items).pipe(delay(100),
                    mergeMap(item => {
                        return this.getChildren({pcode: item.code}).pipe(map(children => {
                            item.children = children;
                            return item;
                        }));
                    }), retry(3));
            }), toArray(), retry(3));
    }

    getChildren(request: Menu): Observable<Menu[]> {
        let params = new HttpParams({fromObject: request as any});
        return this.http.get<Menu[]>('/menus/me', {params: params});
    }
}

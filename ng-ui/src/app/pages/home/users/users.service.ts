import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { User } from './user.types';
import { Page, Pageable } from '@app/core/types';
import { environment } from '@environment/environment';
import { MenuType } from '../menus/menu.types';

@Injectable({
  providedIn: 'root',
})
export class UsersService {
  private readonly API_PREFIX = environment.secApiPath;

  constructor(private http: HttpClient) {}

  page(request: User, page: Pageable): Observable<Page<User>> {
    let params = new HttpParams({ fromObject: request as never });
    params = params.appendAll({ page: page.page - 1, size: page.size });
    for (const sort in page.sorts) {
      params = params.appendAll({ sort: page.sorts[sort] });
    }
    return this.http.get<Page<User>>(this.API_PREFIX + '/users/page', { params: params });
  }

  add(request: User): Observable<User> {
    return this.http.post<User>(this.API_PREFIX + '/users/add', request);
  }

  modify(request: User): Observable<User> {
    return this.http.put<User>(this.API_PREFIX + '/users/modify', request);
  }

  delete(request: User): Observable<User> {
    return this.http.delete<User>(this.API_PREFIX + '/users/delete', { body: request });
  }
}

import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { User } from './user.types';
import { Page, Pageable } from '@app/core/types';

@Injectable({
  providedIn: 'root',
})
export class UsersService {
  constructor(private http: HttpClient) {}

  pageUsers(request: User, page: Pageable): Observable<Page<User>> {
    let params = new HttpParams({ fromObject: request as never });
    params = params.appendAll({ page: page.page - 1, size: page.size });
    for (const sort in page.sorts) {
      params = params.appendAll({ sort: page.sorts[sort] });
    }
    return this.http.get<Page<User>>('/users/page', { params: params });
  }
}

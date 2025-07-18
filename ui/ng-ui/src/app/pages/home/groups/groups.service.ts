import {HttpClient, HttpParams} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Group} from './groups.types';
import {Observable, retry} from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class GroupsService {
  constructor(private http: HttpClient) {
  }

  getGroups(request: Group): Observable<Group[]> {
    const params = new HttpParams({ fromObject: request as never });
    return this.http.get<Group[]>('/menus/search', { params: params }).pipe(retry(3));
  }
}

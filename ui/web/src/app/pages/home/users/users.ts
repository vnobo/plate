import { afterNextRender, Component, inject, signal } from '@angular/core';
import { delay, tap } from 'rxjs';

import { CommonModule } from '@angular/common';
import { HttpClient, HttpParams } from '@angular/common/http';
import { MessageService, ModalsService } from '@app/plugins';
import { Page, Pageable } from '@plate/types';
import { UserForm } from './user-form';
import { User } from './user.types';

@Component({
  selector: 'app-users',
  imports: [CommonModule],
  templateUrl: './users.html',
  styleUrl: './users.scss',
})
export class Users {
  private readonly _message = inject(MessageService);
  private readonly _modal = inject(ModalsService);
  private readonly API_PREFIX = '/sec';
  pageable = signal<Pageable>({
    page: 1,
    size: 10,
    sorts: ['id,desc'],
  });
  userData = signal({} as Page<User>);
  search = signal({
    pcode: '0',
  } as User);

  constructor(private _http: HttpClient) {
    afterNextRender(() => {
      console.log('users service init');
    });
  }

  openModal() {
    this._modal.create({
      title: '用户表单',
      contentRef: UserForm,
    });
  }

  fetchUserData() {
    this.loadData(this.search(), this.pageable()).subscribe(() =>
      this._message.success('数据加载成功!'),
    );
  }

  onTableQueryChange($event: Pageable) {}

  openUserForm(user: User) {
    const modal = this._modal.create({
      contentRef: UserForm,
    });
  }

  onDelete(user: User) {
    this.delete(user)
      .pipe(
        tap(() => this._message.success('删除成功!')),
        delay(1500),
      )
      .subscribe(() => this.fetchUserData());
  }

  page(request: User, page: Pageable) {
    let params = new HttpParams({ fromObject: request as never });
    params = params.appendAll({ page: page.page - 1, size: page.size });
    for (const sort in page.sorts) {
      params = params.appendAll({ sort: page.sorts[sort] });
    }
    return this._http.get<Page<User>>(this.API_PREFIX + '/users/page', { params: params });
  }

  private loadData(search: User, page: Pageable) {
    return this.page(search, page).pipe(tap(result => this.userData.set(result)));
  }

  private add(request: User) {
    return this._http.post<User>(this.API_PREFIX + '/users/add', request);
  }

  private modify(request: User) {
    return this._http.put<User>(this.API_PREFIX + '/users/modify', request);
  }

  private delete(request: User) {
    return this._http.delete<User>(this.API_PREFIX + '/users/delete', { body: request });
  }
}

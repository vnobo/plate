import {Component, inject, signal} from '@angular/core';
import {delay, tap} from 'rxjs';

import {CommonModule} from '@angular/common';
import {HttpClient, HttpParams} from '@angular/common/http';
import {MessageService} from '@app/plugins';
import {Page, Pageable} from '@plate/types';
import {UserForm} from './user-form';
import {User} from './user.types';

@Component({
  selector: 'app-users',
  imports: [CommonModule],
  templateUrl: './users.html',
  styleUrl: './users.scss',
})
export class UsersComponent {
  pageable = signal<Pageable>({
    page: 1,
    size: 10,
    sorts: ['id,desc'],
  });
  private readonly _message = inject(MessageService);
  private readonly _modal = inject(ModalService);

  userData = signal({} as Page<User>);
  private readonly API_PREFIX = '/sec';
  search = signal({
    pcode: '0',
  } as User);

  constructor(private _http: HttpClient) {}

  fetchUserData() {
    this.loadData(this.search(), this.pageable()).subscribe(() =>
      this._message.success('数据加载成功!'),
    );
  }

  onTableQueryChange($event) {
    this.pageable().sorts = ['id,desc'];
    for (const item of $event.sort) {
      if (item.value) {
        const sort = item.key + ',' + (item.value == 'descend' ? 'desc' : 'asc');
        this.pageable().sorts.push(sort);
      }
    }
    this.fetchUserData();
  }

  openUserForm(user: User) {
    const modal = this._modal.create<UserFormComponent, User>({
      nzTitle: '用户表单',
      nzContent: UserForm,
      nzFooter: null,
      nzZIndex: 2000,
    });
    const ref = modal.getContentComponent();
    ref.userData.set(user);
    ref.formSubmit.subscribe(us => {
      if (us.code) {
        this.modify(us).subscribe(res => {
          modal.close();
          this._message.success('修改成功!');
          this.fetchUserData();
        });
      } else {
        this.add(us).subscribe(res => {
          modal.close();
          this._message.success('添加成功!');
          this.fetchUserData();
        });
      }
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

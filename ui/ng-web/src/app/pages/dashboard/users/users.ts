import {
  afterNextRender,
  ChangeDetectionStrategy,
  Component,
  inject,
  inputBinding,
  signal,
} from '@angular/core';
import { delay, tap } from 'rxjs';

import { CommonModule } from '@angular/common';
import { HttpClient, HttpParams } from '@angular/common/http';
import { MessageService, ModalsService } from '@app/plugins';
import { Page, Pageable } from '@plate/types';
import { UserForm } from './user-form';
import { User } from './user.types';
import { environment } from '@envs/env';

@Component({
  selector: 'app-users',
  imports: [CommonModule],
  templateUrl: './users.html',
  styleUrl: './users.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class Users {
  private readonly _message = inject(MessageService);
  private readonly _modal = inject(ModalsService);
  private readonly _http = inject(HttpClient);
  pageable = signal<Pageable>({
    page: 1,
    size: 10,
    sorts: ['id,desc'],
  });
  userData = signal<Page<User>>({
    content: [],
    pageable: {
      page: 0,
      size: 0,
      sorts: [],
    },
    totalElements: 0,
    totalPages: 0,
    size: 0,
    number: 0,
    first: true,
    last: true,
    numberOfElements: 0,
    empty: true,
  });
  search = signal<User>({});

  // Expose Math to template
  Math = Math;

  constructor() {
    afterNextRender(() => {
      // Load initial data
      this.fetchUserData();
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
    // Create a signal with the user data
    const userSignal = signal(user);

    const modal = this._modal.create({
      title: user.id ? '编辑用户' : '添加用户',
      contentRef: UserForm,
      // Pass user data through contentBindings
      contentBindings: [inputBinding('inputData', userSignal)],
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

  // Pagination methods
  changePage(page: number) {
    if (page < 1 || page > this.getTotalPages()) {
      return;
    }

    this.pageable.update(p => ({
      ...p,
      page: page,
    }));

    this.fetchUserData();
  }

  getPageNumbers(): number[] {
    const totalPages = this.getTotalPages();
    const currentPage = this.pageable().page;
    const pages: number[] = [];

    // Show first page
    if (totalPages >= 1) {
      pages.push(1);
    }

    // Show ellipsis if needed
    if (currentPage > 3) {
      pages.push(-1); // -1 represents ellipsis
    }

    // Show pages around current page
    for (
      let i = Math.max(2, currentPage - 1);
      i <= Math.min(totalPages - 1, currentPage + 1);
      i++
    ) {
      if (i > 1 && i < totalPages) {
        pages.push(i);
      }
    }

    // Show ellipsis if needed
    if (currentPage < totalPages - 2) {
      pages.push(-1); // -1 represents ellipsis
    }

    // Show last page
    if (totalPages > 1) {
      pages.push(totalPages);
    }

    return pages;
  }

  getTotalPages(): number {
    const totalElements = this.userData().totalElements || 0;
    const size = this.pageable().size;
    return Math.ceil(totalElements / size);
  }

  page(request: User, page: Pageable) {
    let params = new HttpParams({ fromObject: request as never });
    params = params.appendAll({ page: page.page - 1, size: page.size });
    for (const sort in page.sorts) {
      params = params.appendAll({ sort: page.sorts[sort] });
    }
    // 使用与其它方法一致的 API 前缀，避免未定义的 secApiPath
    return this._http.get<Page<User>>(environment.secApiPath + '/users/page', { params: params });
  }

  private loadData(search: User, page: Pageable) {
    return this.page(search, page).pipe(tap(result => this.userData.set(result)));
  }

  private add(request: User) {
    return this._http.post<User>(environment.secApiPath + '/users/add', request);
  }

  private modify(request: User) {
    return this._http.put<User>(environment.secApiPath + '/users/modify', request);
  }

  private delete(request: User) {
    return this._http.delete<User>(environment.secApiPath + '/users/delete', { body: request });
  }
}

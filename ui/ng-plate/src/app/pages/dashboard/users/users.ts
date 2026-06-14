import { Component, computed, inject, inputBinding, signal } from '@angular/core';
import { delay, tap } from 'rxjs';

import { DatePipe } from '@angular/common';
import { HttpClient, httpResource } from '@angular/common/http';
import { MessageService, ModalsService } from '@app/plugins';
import { Page, Pageable } from '@plate/types';
import { UserForm } from './user-form';
import { User } from './user.types';
import { environment } from '@envs/env';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

@Component({
  selector: 'app-users',
  imports: [DatePipe],
  templateUrl: './users.html',
  styleUrl: './users.scss',
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

  search = signal<User>({});

  private readonly emptyPage: Page<User> = {
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
  };

  protected readonly userResource = httpResource<Page<User>>(
    () => {
      const search = this.search();
      const page = this.pageable();
      const params: Record<
        string,
        string | number | boolean | ReadonlyArray<string | number | boolean>
      > = {
        ...(search as Record<
          string,
          string | number | boolean | ReadonlyArray<string | number | boolean>
        >),
        page: page.page - 1,
        size: page.size,
      };
      for (const sort of page.sorts) {
        if (!params['sort']) {
          params['sort'] = [sort];
        } else if (Array.isArray(params['sort'])) {
          (params['sort'] as string[]).push(sort);
        }
      }
      return {
        url: environment.secApiPath + '/users/page',
        params,
      };
    },
    {
      defaultValue: this.emptyPage,
      debugName: 'users-page',
    },
  );

  userData = computed(() => this.userResource.value());

  isLoading = computed(() => this.userResource.isLoading());

  Math = Math;

  openModal() {
    this._modal.create({
      title: '用户表单',
      contentRef: UserForm,
    });
  }

  fetchUserData() {
    this.userResource.reload();
  }

  onTableQueryChange($event: Pageable) {}

  openUserForm(user: User) {
    const userSignal = signal(user);

    this._modal.create({
      title: user.id ? '编辑用户' : '添加用户',
      contentRef: UserForm,
      contentBindings: [inputBinding('inputData', userSignal)],
    });
  }

  onDelete(user: User) {
    this.delete(user)
      .pipe(
        tap(() => this._message.success('删除成功!')),
        delay(1500),
        takeUntilDestroyed(),
      )
      .subscribe(() => this.fetchUserData());
  }

  changePage(page: number) {
    if (page < 1 || page > this.getTotalPages()) {
      return;
    }

    this.pageable.update((p) => ({
      ...p,
      page: page,
    }));
  }

  getPageNumbers(): number[] {
    const totalPages = this.getTotalPages();
    const currentPage = this.pageable().page;
    const pages: number[] = [];

    if (totalPages >= 1) {
      pages.push(1);
    }

    if (currentPage > 3) {
      pages.push(-1);
    }

    for (
      let i = Math.max(2, currentPage - 1);
      i <= Math.min(totalPages - 1, currentPage + 1);
      i++
    ) {
      if (i > 1 && i < totalPages) {
        pages.push(i);
      }
    }

    if (currentPage < totalPages - 2) {
      pages.push(-1);
    }

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

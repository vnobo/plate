import { Component, computed, inject, signal } from '@angular/core';
import { delay, tap } from 'rxjs';

import { DatePipe } from '@angular/common';
import { HttpClient, httpResource } from '@angular/common/http';
import { DestroyRef } from '@angular/core';
import { outputToObservable, takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MessageService, ModalsService } from '@app/plugins';
import { Pagination } from '@app/shared/pagination';
import { Page, Pageable } from '@plate/types';
import { UserForm } from './user-form';
import { UserAuthorityForm } from './user-authority-form';
import { User } from './user.types';
import { environment } from '@envs/env';

/** Filterable user fields exposed by the search bar. */
const USER_SEARCH_FIELDS = [
  { value: 'username', label: '用户名' },
  { value: 'name', label: '昵称' },
  { value: 'email', label: '电子邮件' },
  { value: 'phone', label: '手机号' },
] as const;

type SearchField = (typeof USER_SEARCH_FIELDS)[number]['value'];

interface UserFilters {
  field: SearchField;
  keyword: string;
  status: 'all' | 'enabled' | 'disabled';
}

const INITIAL_FILTERS: UserFilters = { field: 'username', keyword: '', status: 'all' };

@Component({
  selector: 'app-users',
  imports: [DatePipe, Pagination],
  templateUrl: './users.html',
  styleUrl: './users.scss',
})
export class Users {
  private readonly _message = inject(MessageService);
  private readonly _modal = inject(ModalsService);
  private readonly _http = inject(HttpClient);
  private readonly _destroyRef = inject(DestroyRef);

  protected readonly searchFields = USER_SEARCH_FIELDS;

  pageable = signal<Pageable>({
    page: 1,
    size: 10,
    sorts: ['id,desc'],
  });

  /** Draft filter inputs bound to the search bar controls. */
  protected readonly draft = signal<UserFilters>({ ...INITIAL_FILTERS });

  /** Applied filters driving the query; refreshed by 查询 / 重置. */
  private readonly applied = signal<UserFilters>({ ...INITIAL_FILTERS });

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
      const filters = this.applied();
      const page = this.pageable();
      const params: Record<
        string,
        string | number | boolean | ReadonlyArray<string | number | boolean>
      > = {
        page: page.page - 1,
        size: page.size,
      };
      const keyword = filters.keyword.trim();
      if (keyword) {
        // Backend string criteria use LIKE matching; wrap in wildcards for fuzzy search.
        params[filters.field] = `%${keyword}%`;
      }
      if (filters.status !== 'all') {
        params['disabled'] = filters.status === 'disabled';
      }
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

  openModal() {
    this.openUserForm({} as User);
  }

  fetchUserData() {
    this.userResource.reload();
  }

  openUserForm(user: User) {
    const ref = this._modal.create({
      title: user.id ? '编辑用户' : '添加用户',
      contentRef: UserForm,
      contentInputs: { inputData: user },
    });
    outputToObservable(ref.instance.dropped)
      .pipe(takeUntilDestroyed(this._destroyRef))
      .subscribe(() => this.fetchUserData());
  }

  /** Opens the direct-authority management modal for a user. */
  openAuthorityForm(user: User) {
    if (!user.code) {
      return;
    }
    this._modal.create({
      title: `分配权限 - ${user.name || user.username || ''}`,
      contentRef: UserAuthorityForm,
      contentInputs: { userCode: user.code, userName: user.name ?? '' },
    });
  }

  onDelete(user: User) {
    const label = user.name || user.username || user.code || '';
    if (!confirm(`确定删除用户「${label}」吗？此操作不可撤销。`)) {
      return;
    }
    this.delete(user)
      .pipe(
        tap(() => this._message.success('删除成功!')),
        delay(1500),
        takeUntilDestroyed(this._destroyRef),
      )
      .subscribe(() => this.fetchUserData());
  }

  onDraftField(event: Event) {
    const value = (event.target as HTMLSelectElement).value as SearchField;
    this.draft.update((d) => ({ ...d, field: value }));
  }

  onDraftKeyword(event: Event) {
    const value = (event.target as HTMLInputElement).value;
    this.draft.update((d) => ({ ...d, keyword: value }));
  }

  onDraftStatus(event: Event) {
    const value = (event.target as HTMLSelectElement).value as UserFilters['status'];
    this.draft.update((d) => ({ ...d, status: value }));
  }

  /** Applies the draft filters and jumps back to the first page. */
  applySearch() {
    this.applied.set({ ...this.draft() });
    this.pageable.update((p) => ({ ...p, page: 1 }));
  }

  resetSearch() {
    this.draft.set({ ...INITIAL_FILTERS });
    this.applied.set({ ...INITIAL_FILTERS });
    this.pageable.update((p) => ({ ...p, page: 1 }));
  }

  changePage(page: number) {
    this.pageable.update((p) => ({ ...p, page }));
  }

  changeSize(size: number) {
    this.pageable.update((p) => ({ ...p, size, page: 1 }));
  }

  private delete(request: User) {
    return this._http.delete<User>(environment.secApiPath + '/users/delete', { body: request });
  }
}

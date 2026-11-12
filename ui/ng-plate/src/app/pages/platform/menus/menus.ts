import { DatePipe } from '@angular/common';
import { HttpClient, httpResource } from '@angular/common/http';
import { Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { outputToObservable, takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MessageService, ModalsService } from '@app/plugins';
import { Pagination } from '@app/shared/pagination';
import { Page, Pageable } from '@plate/types';
import { delay, tap } from 'rxjs';

import { environment } from '@envs/env';
import { MenuForm } from './menu-form';
import { MenuItem, MenuType, MENU_TYPES } from './menus.types';

interface MenuFilters {
  keyword: string;
  type: 'all' | MenuType;
}

const INITIAL_FILTERS: MenuFilters = { keyword: '', type: 'all' };

/** Type badge styles. */
const TYPE_BADGES: Record<MenuType, { label: string; className: string }> = {
  FOLDER: { label: 'Group', className: 'bg-secondary' },
  MENU: { label: 'Menu', className: 'bg-primary' },
  LINK: { label: 'External Link', className: 'bg-info' },
  API: { label: 'API', className: 'bg-warning' },
};

@Component({
  selector: 'app-menus',
  imports: [DatePipe, Pagination],
  templateUrl: './menus.html',
})
export class Menus {
  private readonly _message = inject(MessageService);
  private readonly _modal = inject(ModalsService);
  private readonly _http = inject(HttpClient);
  private readonly _destroyRef = inject(DestroyRef);

  protected readonly menuTypes = MENU_TYPES;
  protected readonly typeBadges = TYPE_BADGES;

  pageable = signal<Pageable>({
    page: 1,
    size: 10,
    // The backend MenusService always orders by sortNo ascending, so we do not send an extra sort.
    sorts: [],
  });

  /** Draft values bound to the search bar. */
  protected readonly draft = signal<MenuFilters>({ ...INITIAL_FILTERS });

  /** Applied filters that actually drive the query; refreshed by Search / Reset. */
  private readonly applied = signal<MenuFilters>({ ...INITIAL_FILTERS });

  private readonly emptyPage: Page<MenuItem> = {
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

  protected readonly menuResource = httpResource<Page<MenuItem>>(
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
        // Backend string criteria use LIKE matching, so wrap with wildcards for fuzzy search.
        params['name'] = `%${keyword}%`;
      }
      if (filters.type !== 'all') {
        params['type'] = filters.type;
      }
      return {
        url: environment.relaApiPath + '/menus/page',
        params,
      };
    },
    {
      defaultValue: this.emptyPage,
      debugName: 'menus-page',
    },
  );

  /** All menus, used to pick a parent in the add/edit form. */
  private readonly allMenusResource = httpResource<MenuItem[]>(
    () => ({
      url: environment.relaApiPath + '/menus/search',
      params: { size: 1000 },
    }),
    { defaultValue: [], debugName: 'menus-all' },
  );

  menuData = computed(() => this.menuResource.value());

  isLoading = computed(() => this.menuResource.isLoading());

  /** Reading value() in an error state throws, so check hasValue() first. */
  protected readonly menus = computed(() =>
    this.allMenusResource.hasValue() ? this.allMenusResource.value() : [],
  );

  openModal() {
    this.openMenuForm({} as MenuItem);
  }

  fetchMenuData() {
    this.menuResource.reload();
    this.allMenusResource.reload();
  }

  openMenuForm(menu: MenuItem) {
    const ref = this._modal.create({
      title: menu.id ? 'Edit Menu' : 'Add Menu',
      contentRef: MenuForm,
      contentInputs: { inputData: menu, allMenus: this.menus() },
    });
    outputToObservable(ref.instance.dropped)
      .pipe(takeUntilDestroyed(this._destroyRef))
      .subscribe(() => this.fetchMenuData());
  }

  onDelete(menu: MenuItem) {
    const label = menu.name || menu.authority || menu.code || '';
    this._modal
      .confirm({
        title: 'Delete Menu',
        message: `Are you sure you want to delete menu "${label}"? This action cannot be undone.`,
        confirmText: 'Delete',
        danger: true,
      })
      .then((ok) => {
        if (!ok) {
          return;
        }
        this.delete(menu)
          .pipe(
            tap(() => this._message.success('Deleted!')),
            delay(1500),
            takeUntilDestroyed(this._destroyRef),
          )
          .subscribe(() => this.fetchMenuData());
      });
  }

  onDraftKeyword(event: Event) {
    const value = (event.target as HTMLInputElement).value;
    this.draft.update((d) => ({ ...d, keyword: value }));
  }

  onDraftType(event: Event) {
    const value = (event.target as HTMLSelectElement).value as MenuFilters['type'];
    this.draft.update((d) => ({ ...d, type: value }));
  }

  /** Applies the draft filters and returns to the first page. */
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

  private delete(request: MenuItem) {
    return this._http.delete<MenuItem>(environment.relaApiPath + '/menus/delete', {
      body: request,
    });
  }
}

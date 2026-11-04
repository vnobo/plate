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

/** 类型徽标 */
const TYPE_BADGES: Record<MenuType, { label: string; className: string }> = {
  FOLDER: { label: '分组', className: 'bg-secondary' },
  MENU: { label: '菜单', className: 'bg-primary' },
  LINK: { label: '外链', className: 'bg-info' },
  API: { label: '接口', className: 'bg-warning' },
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
    // 后端 MenusService 固定按 sortNo 升序，这里不额外下发 sort
    sorts: [],
  });

  /** 搜索栏的草稿值 */
  protected readonly draft = signal<MenuFilters>({ ...INITIAL_FILTERS });

  /** 真正驱动查询的筛选条件，由 查询 / 重置 刷新 */
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
        // 后端字符串条件走 LIKE 匹配，包一层通配符做模糊搜索
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

  /** 全部菜单，给新增/编辑表单选父级用 */
  private readonly allMenusResource = httpResource<MenuItem[]>(
    () => ({
      url: environment.relaApiPath + '/menus/search',
      params: { size: 1000 },
    }),
    { defaultValue: [], debugName: 'menus-all' },
  );

  menuData = computed(() => this.menuResource.value());

  isLoading = computed(() => this.menuResource.isLoading());

  /** 错误态读 value() 会抛错，先问 hasValue() */
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
      title: menu.id ? '编辑菜单' : '添加菜单',
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
        title: '删除菜单',
        message: `确定删除菜单「${label}」吗？此操作不可撤销。`,
        confirmText: '删除',
        danger: true,
      })
      .then((ok) => {
        if (!ok) {
          return;
        }
        this.delete(menu)
          .pipe(
            tap(() => this._message.success('删除成功!')),
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

  /** 应用草稿筛选并回到第一页 */
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

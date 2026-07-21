import { DatePipe } from '@angular/common';
import { HttpClient, httpResource } from '@angular/common/http';
import { Component, computed, DestroyRef, inject, signal } from '@angular/core';
import {
  FieldState,
  FormField,
  form,
  maxLength,
  minLength,
  required,
  submit,
} from '@angular/forms/signals';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { delay, tap } from 'rxjs';

import { MessageService } from '@app/plugins';
import { Page, Pageable } from '@plate/types';
import { environment } from '@envs/env';

import { ROOT_PCODE, Tenant } from './tenant.types';

@Component({
  selector: 'app-tenant',
  imports: [DatePipe, FormField],
  templateUrl: './tenant.html',
  styleUrl: './tenant.scss',
})
export class Tenants {
  private readonly _message = inject(MessageService);
  private readonly _http = inject(HttpClient);
  private readonly _destroyRef = inject(DestroyRef);

  protected readonly ROOT_PCODE = ROOT_PCODE;

  pageable = signal<Pageable>({
    page: 1,
    size: 10,
    sorts: ['id,desc'],
  });

  /** 名称关键字（后端按 name 模糊匹配） */
  protected readonly searchKeyword = signal('');

  private readonly emptyPage: Page<Tenant> = {
    content: [],
    pageable: { page: 0, size: 0, sorts: [] },
    totalElements: 0,
    totalPages: 0,
    size: 0,
    number: 0,
    first: true,
    last: true,
    numberOfElements: 0,
    empty: true,
  };

  protected readonly tenantResource = httpResource<Page<Tenant>>(
    () => {
      const page = this.pageable();
      const keyword = this.searchKeyword().trim();
      const params: Record<
        string,
        string | number | boolean | ReadonlyArray<string | number | boolean>
      > = {
        page: page.page - 1,
        size: page.size,
      };
      if (keyword) {
        params['name'] = keyword;
      }
      for (const sort of page.sorts) {
        if (!params['sort']) {
          params['sort'] = [sort];
        } else if (Array.isArray(params['sort'])) {
          (params['sort'] as string[]).push(sort);
        }
      }
      return {
        url: environment.secApiPath + '/tenants/page',
        params,
      };
    },
    {
      defaultValue: this.emptyPage,
      debugName: 'tenants-page',
    },
  );

  /** 全部租户（用于父级下拉与名称解析） */
  protected readonly parentResource = httpResource<Tenant[]>(
    () => ({
      url: environment.secApiPath + '/tenants/search',
      params: { size: 1000 },
    }),
    {
      defaultValue: [],
      debugName: 'tenants-parent',
    },
  );

  protected readonly tenantData = computed(() => this.tenantResource.value());
  protected readonly isLoading = computed(() => this.tenantResource.isLoading());
  protected readonly parents = computed(() => this.parentResource.value() ?? []);

  protected readonly parentNameMap = computed(() => {
    const map = new Map<string, string>();
    for (const t of this.parents()) {
      if (t.code) {
        map.set(t.code, t.name ?? t.code);
      }
    }
    return map;
  });

  protected parentName(code?: string): string {
    if (!code) return '-';
    if (code === ROOT_PCODE) return '根租户';
    return this.parentNameMap().get(code) ?? code;
  }

  protected readonly Math = Math;

  protected readonly isEditing = signal(false);
  protected readonly currentTenant = signal<Tenant | null>(null);

  private readonly initialModel = {
    name: '',
    description: '',
    pcode: ROOT_PCODE,
  };

  protected readonly tenantModel = signal({ ...this.initialModel });

  protected readonly tenantForm = form(this.tenantModel, (p) => {
    required(p.name, { message: '租户名称 是必填项' });
    minLength(p.name, 2, { message: '租户名称 至少需要 2 个字符' });
    maxLength(p.name, 50, { message: '租户名称 不能超过 50 个字符' });
    maxLength(p.description, 500, { message: '描述 不能超过 500 个字符' });
    required(p.pcode, { message: '父级租户 是必填项' });
  });

  protected getFieldError(fieldName: string): string {
    const fieldState = (this.tenantForm as unknown as Record<string, () => FieldState<unknown>>)[
      fieldName
    ]?.();
    if (!fieldState) return '';
    if (!fieldState.invalid() || !fieldState.touched()) return '';
    const errors = fieldState.errors();
    if (!errors) return '';
    for (const error of errors) {
      if (error.message) return error.message;
    }
    return '输入有误';
  }

  protected isFieldInvalid(fieldName: string): boolean {
    const fieldState = (this.tenantForm as unknown as Record<string, () => FieldState<unknown>>)[
      fieldName
    ]?.();
    if (!fieldState) return false;
    return fieldState.invalid() && fieldState.touched();
  }

  protected createTenant(): void {
    this.currentTenant.set(null);
    this.tenantModel.set({ ...this.initialModel });
    this.isEditing.set(true);
  }

  protected editTenant(tenant: Tenant): void {
    this.currentTenant.set(tenant);
    this.tenantModel.set({
      name: tenant.name ?? '',
      description: tenant.description ?? '',
      pcode: tenant.pcode ?? ROOT_PCODE,
    });
    this.isEditing.set(true);
  }

  protected cancelEdit(): void {
    this.isEditing.set(false);
    this.currentTenant.set(null);
    this.tenantModel.set({ ...this.initialModel });
  }

  protected async saveTenant(): Promise<void> {
    await submit(this.tenantForm, {
      action: async () => {
        const model = this.tenantModel();
        const current = this.currentTenant();
        const payload: Tenant = {
          name: model.name,
          description: model.description,
          pcode: model.pcode,
        };
        if (current?.code) {
          payload.id = current.id;
          payload.code = current.code;
        }

        const request = current?.code
          ? this._http.put<Tenant>(environment.secApiPath + '/tenants/save', payload)
          : this._http.post<Tenant>(environment.secApiPath + '/tenants/save', payload);

        request
          .pipe(
            tap(() => this._message.success(current?.code ? '租户已更新' : '租户已创建')),
            delay(800),
            takeUntilDestroyed(this._destroyRef),
          )
          .subscribe(() => {
            this.cancelEdit();
            this.tenantResource.reload();
            this.parentResource.reload();
          });
      },
    });
  }

  protected onDelete(tenant: Tenant): void {
    if (!tenant.code || tenant.id == null) {
      return;
    }
    if (!confirm(`确定要删除租户「${tenant.name ?? tenant.code}」吗？此操作不可撤销。`)) {
      return;
    }
    this._http
      .delete<void>(environment.secApiPath + '/tenants/delete', {
        body: { id: tenant.id, code: tenant.code },
      })
      .pipe(
        tap(() => this._message.success('租户已删除')),
        delay(800),
        takeUntilDestroyed(this._destroyRef),
      )
      .subscribe(() => {
        this.tenantResource.reload();
        this.parentResource.reload();
      });
  }

  protected onSearchChange(value: string): void {
    this.searchKeyword.set(value);
    this.pageable.update((p) => ({ ...p, page: 1 }));
  }

  protected changePage(page: number): void {
    if (page < 1 || page > this.getTotalPages()) {
      return;
    }
    this.pageable.update((p) => ({ ...p, page }));
  }

  protected getTotalPages(): number {
    const totalElements = this.tenantData().totalElements || 0;
    return Math.ceil(totalElements / this.pageable().size);
  }

  protected getPageNumbers(): number[] {
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
}

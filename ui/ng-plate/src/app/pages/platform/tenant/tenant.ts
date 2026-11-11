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

import { MessageService, ModalsService } from '@app/plugins';
import { Pagination } from '@app/shared/pagination';
import { Page, Pageable } from '@plate/types';
import { environment } from '@envs/env';

import { ROOT_PCODE, Tenant } from './tenant.types';
import { TenantMembersForm } from './tenant-members-form';

@Component({
  selector: 'app-tenant',
  imports: [DatePipe, FormField, Pagination],
  templateUrl: './tenant.html',
  styleUrl: './tenant.scss',
})
export class Tenants {
  private readonly _message = inject(MessageService);
  private readonly _modal = inject(ModalsService);
  private readonly _http = inject(HttpClient);
  private readonly _destroyRef = inject(DestroyRef);

  protected readonly ROOT_PCODE = ROOT_PCODE;

  pageable = signal<Pageable>({
    page: 1,
    size: 10,
    sorts: ['id,desc'],
  });

  /** Name keyword (backend matches against name with fuzzy search) */
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
        // Backend string criteria use LIKE matching; wrap in wildcards for fuzzy search.
        params['name'] = `%${keyword}%`;
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

  /** All tenants (used for the parent dropdown and name resolution) */
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
    if (code === ROOT_PCODE) return 'Root Tenant';
    return this.parentNameMap().get(code) ?? code;
  }

  protected readonly isEditing = signal(false);
  protected readonly currentTenant = signal<Tenant | null>(null);

  private readonly initialModel = {
    name: '',
    description: '',
    pcode: ROOT_PCODE,
  };

  protected readonly tenantModel = signal({ ...this.initialModel });

  protected readonly tenantForm = form(this.tenantModel, (p) => {
    required(p.name, { message: 'Tenant name is required' });
    minLength(p.name, 2, { message: 'Tenant name must be at least 2 characters' });
    maxLength(p.name, 50, { message: 'Tenant name cannot exceed 50 characters' });
    maxLength(p.description, 500, { message: 'Description cannot exceed 500 characters' });
    required(p.pcode, { message: 'Parent tenant is required' });
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
    return 'Invalid input';
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
            tap(() => this._message.success(current?.code ? 'Tenant updated' : 'Tenant created')),
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
    if (!confirm(`Are you sure you want to delete tenant "${tenant.name ?? tenant.code}"? This action cannot be undone.`)) {
      return;
    }
    this._http
      .delete<void>(environment.secApiPath + '/tenants/delete', {
        body: { id: tenant.id, code: tenant.code },
      })
      .pipe(
        tap(() => this._message.success('Tenant deleted')),
        delay(800),
        takeUntilDestroyed(this._destroyRef),
      )
      .subscribe(() => {
        this.tenantResource.reload();
        this.parentResource.reload();
      });
  }

  protected onSearchChange(event: Event): void {
    this.searchKeyword.set((event.target as HTMLInputElement).value);
    this.pageable.update((p) => ({ ...p, page: 1 }));
  }

  /** Opens the member management modal for a tenant. */
  protected openMembersForm(tenant: Tenant): void {
    if (!tenant.code) {
      return;
    }
    this._modal.create({
      title: `Member Management - ${tenant.name || tenant.code}`,
      contentRef: TenantMembersForm,
      contentInputs: { tenantCode: tenant.code, tenantName: tenant.name ?? '' },
    });
  }

  protected changePage(page: number): void {
    this.pageable.update((p) => ({ ...p, page }));
  }

  protected changeSize(size: number): void {
    this.pageable.update((p) => ({ ...p, size, page: 1 }));
  }
}

import { DatePipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import {
  FieldState,
  FormField,
  form,
  maxLength,
  minLength,
  required,
  submit,
} from '@angular/forms/signals';
import { Router } from '@angular/router';

export interface Tenant {
  id: string;
  name: string;
  description?: string;
  status: 'active' | 'inactive' | 'suspended';
  createdAt: Date;
  updatedAt: Date;
  subscriptionType?: string;
  expirationDate?: Date;
}

@Component({
  selector: 'app-tenant',
  imports: [DatePipe, FormField],
  templateUrl: './tenant.html',
  styleUrl: './tenant.scss',
})
export class Tenants {
  protected readonly tenants = signal<Tenant[]>([
    {
      id: '1',
      name: '示例租户1',
      description: '这是一个示例租户1',
      status: 'active',
      createdAt: new Date('2023-01-15'),
      updatedAt: new Date('2023-10-20'),
      subscriptionType: 'premium',
      expirationDate: new Date('2024-01-15'),
    },
    {
      id: '2',
      name: '示例租户2',
      description: '这是一个示例租户2',
      status: 'inactive',
      createdAt: new Date('2023-03-02'),
      updatedAt: new Date('2023-09-10'),
      subscriptionType: 'standard',
      expirationDate: new Date('2023-12-31'),
    },
    {
      id: '3',
      name: '示例租户3',
      description: '这是一个示例租户3',
      status: 'suspended',
      createdAt: new Date('2023-05-30'),
      updatedAt: new Date('2023-11-05'),
      subscriptionType: 'premium',
      expirationDate: new Date('2024-02-28'),
    },
  ]);

  protected readonly currentPage = signal(1);
  protected readonly itemsPerPage = signal(5);
  protected readonly totalPages = computed(() =>
    Math.ceil(this.filteredTenants().length / this.itemsPerPage()),
  );

  protected readonly paginatedTenants = computed(() => {
    const start = (this.currentPage() - 1) * this.itemsPerPage();
    const end = start + this.itemsPerPage();
    return this.filteredTenants().slice(start, end);
  });

  protected readonly searchKeyword = signal('');

  protected readonly filteredTenants = computed(() => {
    const keyword = this.searchKeyword().toLowerCase();
    return this.tenants().filter(
      (tenant) =>
        tenant.name.toLowerCase().includes(keyword) ||
        (tenant.description && tenant.description.toLowerCase().includes(keyword)),
    );
  });

  protected readonly currentTenant = signal<Tenant | null>(null);

  protected readonly isEditing = signal(false);

  private readonly router = inject(Router);

  private readonly initialFormModel = {
    name: '',
    description: '',
    status: 'active' as 'active' | 'inactive' | 'suspended',
    subscriptionType: 'standard' as 'basic' | 'standard' | 'premium',
    expirationDate: null as Date | null,
  };

  protected readonly tenantModel = signal({ ...this.initialFormModel });

  protected readonly tenantForm = form(this.tenantModel, (p) => {
    required(p.name, { message: '租户名称 是必填项' });
    minLength(p.name, 2, { message: '租户名称 至少需要 2 个字符' });
    maxLength(p.name, 50, { message: '租户名称 不能超过 50 个字符' });
    maxLength(p.description, 500, { message: '描述 不能超过 500 个字符' });
    required(p.status, { message: '状态 是必填项' });
    required(p.subscriptionType, { message: '订阅类型 是必填项' });
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

  editTenant(tenant: Tenant) {
    this.currentTenant.set(tenant);
    this.tenantModel.set({
      name: tenant.name,
      description: tenant.description || '',
      status: tenant.status,
      subscriptionType: (tenant.subscriptionType ?? 'standard') as 'basic' | 'standard' | 'premium',
      expirationDate: tenant.expirationDate || null,
    });
    this.isEditing.set(true);
  }

  createTenant() {
    this.currentTenant.set(null);
    this.tenantModel.set({ ...this.initialFormModel });
    this.isEditing.set(true);
  }

  async saveTenant() {
    await submit(this.tenantForm, {
      action: async () => {
        const formData = this.tenantModel();

        if (this.currentTenant()) {
          this.tenants.update((tenants) =>
            tenants.map((tenant) =>
              tenant.id === this.currentTenant()?.id
                ? ({
                    ...tenant,
                    name: formData.name ?? tenant.name,
                    description: formData.description ?? tenant.description,
                    status:
                      (formData.status as 'active' | 'inactive' | 'suspended') ?? tenant.status,
                    subscriptionType: formData.subscriptionType ?? tenant.subscriptionType,
                    expirationDate: formData.expirationDate ?? tenant.expirationDate,
                    updatedAt: new Date(),
                  } as Tenant)
                : tenant,
            ),
          );
        } else {
          const newTenant: Tenant = {
            id: (Math.max(...this.tenants().map((t) => parseInt(t.id) || 0), 0) + 1).toString(),
            name: formData.name ?? '',
            description: formData.description ?? '',
            status: (formData.status as 'active' | 'inactive' | 'suspended') ?? 'active',
            createdAt: new Date(),
            updatedAt: new Date(),
            subscriptionType: formData.subscriptionType ?? undefined,
            expirationDate: formData.expirationDate ?? undefined,
          };
          this.tenants.update((tenants) => [...tenants, newTenant]);
        }

        this.isEditing.set(false);
        this.currentTenant.set(null);
        this.tenantModel.set({ ...this.initialFormModel });
      },
    });
  }

  deleteTenant(id: string) {
    if (confirm('确定要删除这个租户吗？此操作不可撤销。')) {
      this.tenants.update((tenants) => tenants.filter((tenant) => tenant.id !== id));
      if (this.paginatedTenants().length === 0 && this.currentPage() > 1) {
        this.currentPage.update((page) => page - 1);
      }
    }
  }

  cancelEdit() {
    this.isEditing.set(false);
    this.currentTenant.set(null);
    this.tenantModel.set({ ...this.initialFormModel });
  }

  onSearchChange(value: string) {
    this.searchKeyword.set(value);
    this.currentPage.set(1);
  }

  getStatusText(status: 'active' | 'inactive' | 'suspended'): string {
    switch (status) {
      case 'active':
        return '活跃';
      case 'inactive':
        return '非活跃';
      case 'suspended':
        return '已暂停';
      default:
        return status;
    }
  }

  getStatusClass(status: 'active' | 'inactive' | 'suspended'): string {
    switch (status) {
      case 'active':
        return 'badge bg-success';
      case 'inactive':
        return 'badge bg-secondary';
      case 'suspended':
        return 'badge bg-warning';
      default:
        return 'badge bg-secondary';
    }
  }

  changePage(page: number): void {
    if (page >= 1 && page <= this.totalPages()) {
      this.currentPage.set(page);
    }
  }

  getPaginationItems(): number[] {
    const total = this.totalPages();
    const current = this.currentPage();
    const items: number[] = [];

    if (total <= 7) {
      for (let i = 1; i <= total; i++) {
        items.push(i);
      }
    } else {
      items.push(1);
      if (current > 4) {
        items.push(-1);
      }
      for (let i = Math.max(2, current - 2); i <= Math.min(total - 1, current + 2); i++) {
        items.push(i);
      }
      if (current < total - 3) {
        items.push(-1);
      }
      items.push(total);
    }

    return items;
  }

  getMinDate(value1: number, value2: number): number {
    return Math.min(value1, value2);
  }
}

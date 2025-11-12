import { Component, ChangeDetectionStrategy, signal, computed, inject } from '@angular/core';
import { Tenant } from './tenant.types';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators, FormControl } from '@angular/forms';
import { NgOptimizedImage } from '@angular/common';
import { Router } from '@angular/router';

@Component({
  selector: 'app-tenant-management',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, NgOptimizedImage],
  templateUrl: './tenant.html',
  styleUrl: './tenant.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TenantManagementComponent {
  // 模拟租户数据
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

  // 分页相关
  protected readonly currentPage = signal(1);
  protected readonly itemsPerPage = signal(5);
  protected readonly totalPages = computed(() =>
    Math.ceil(this.filteredTenants().length / this.itemsPerPage())
  );

  protected readonly paginatedTenants = computed(() => {
    const start = (this.currentPage() - 1) * this.itemsPerPage();
    const end = start + this.itemsPerPage();
    return this.filteredTenants().slice(start, end);
  });

  // 搜索关键词
  protected readonly searchKeyword = signal('');

  // 过滤后的租户列表
  protected readonly filteredTenants = computed(() => {
    const keyword = this.searchKeyword().toLowerCase();
    return this.tenants().filter(
      (tenant) =>
        tenant.name.toLowerCase().includes(keyword) ||
        (tenant.description && tenant.description.toLowerCase().includes(keyword))
    );
  });

  // 当前选中的租户
  protected readonly currentTenant = signal<Tenant | null>(null);

  // 编辑模式
  protected readonly isEditing = signal(false);

  // 注入依赖
  private readonly fb = inject(FormBuilder);
  private readonly router = inject(Router);

  // 表单定义
  protected readonly tenantForm = this.fb.group({
    name: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(50)]],
    description: ['', [Validators.maxLength(500)]],
    status: ['active', [Validators.required]],
    subscriptionType: ['standard', [Validators.required]],
    expirationDate: [null as Date | null, []],
  });

  // 获取表单控件的便捷方法
  protected getFormControl(name: string): FormControl {
    return this.tenantForm.get(name) as FormControl;
  }

  // 检查字段是否有错误
  protected hasError(fieldName: string, errorType?: string): boolean {
    const control = this.tenantForm.get(fieldName);
    if (!control) return false;

    if (errorType) {
      return control.hasError(errorType) && control.touched;
    }
    return control.invalid && control.touched;
  }

  // 获取错误信息
  protected getErrorMessage(fieldName: string): string {
    const control = this.tenantForm.get(fieldName);
    if (!control || !control.errors || !control.touched) return '';

    const errors = control.errors;

    if (errors['required']) {
      return `${fieldName} 是必填项`;
    }
    if (errors['minlength']) {
      return `${fieldName} 至少需要 ${errors['minlength'].requiredLength} 个字符`;
    }
    if (errors['maxlength']) {
      return `${fieldName} 不能超过 ${errors['maxlength'].requiredLength} 个字符`;
    }

    return '输入有误';
  }

  // 打开编辑租户对话框
  editTenant(tenant: Tenant) {
    this.currentTenant.set(tenant);
    this.tenantForm.patchValue({
      name: tenant.name,
      description: tenant.description || '',
      status: tenant.status,
      subscriptionType: tenant.subscriptionType || 'standard',
      expirationDate: tenant.expirationDate || null,
    });
    this.isEditing.set(true);
  }

  // 创建新租户
  createTenant() {
    this.currentTenant.set(null);
    this.tenantForm.reset();
    this.tenantForm.patchValue({
      status: 'active',
      subscriptionType: 'standard',
    });
    this.isEditing.set(true);
  }

  // 保存租户
  saveTenant() {
    if (this.tenantForm.invalid) {
      // 标记所有字段为已触摸以显示验证错误
      this.markFormGroupTouched();
      return;
    }

    const formData = this.tenantForm.value;

    if (this.currentTenant()) {
      // 更新现有租户
      this.tenants.update((tenants) =>
        tenants.map((tenant) =>
          tenant.id === this.currentTenant()?.id
            ? ({
                ...tenant,
                name: formData.name ?? tenant.name,
                description: formData.description ?? tenant.description,
                status: (formData.status as 'active' | 'inactive' | 'suspended') ?? tenant.status,
                subscriptionType: formData.subscriptionType ?? tenant.subscriptionType,
                expirationDate: formData.expirationDate ?? tenant.expirationDate,
                updatedAt: new Date(),
              } as Tenant)
            : tenant
        )
      );
    } else {
      // 创建新租户
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
    this.tenantForm.reset();
  }

  // 删除租户
  deleteTenant(id: string) {
    if (confirm('确定要删除这个租户吗？此操作不可撤销。')) {
      this.tenants.update((tenants) => tenants.filter((tenant) => tenant.id !== id));
      // 如果删除后当前页没有数据且不是第一页，则跳转到上一页
      if (this.paginatedTenants().length === 0 && this.currentPage() > 1) {
        this.currentPage.update((page) => page - 1);
      }
    }
  }

  // 取消编辑
  cancelEdit() {
    this.isEditing.set(false);
    this.currentTenant.set(null);
    this.tenantForm.reset();
  }

  // 更新搜索关键词
  onSearchChange(value: string) {
    this.searchKeyword.set(value);
    // 搜索时返回第一页
    this.currentPage.set(1);
  }

  // 获取状态显示文本
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

  // 获取状态CSS类
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

  // 分页相关方法
  changePage(page: number): void {
    if (page >= 1 && page <= this.totalPages()) {
      this.currentPage.set(page);
    }
  }

  getPaginationItems(): number[] {
    const total = this.totalPages();
    const current = this.currentPage();
    const items: number[] = [];

    // 简单的分页逻辑，显示当前页附近的页码
    if (total <= 7) {
      for (let i = 1; i <= total; i++) {
        items.push(i);
      }
    } else {
      items.push(1);
      if (current > 4) {
        items.push(-1); // 省略标记
      }
      for (let i = Math.max(2, current - 2); i <= Math.min(total - 1, current + 2); i++) {
        items.push(i);
      }
      if (current < total - 3) {
        items.push(-1); // 省略标记
      }
      items.push(total);
    }

    return items;
  }

  // 标记表单组所有控件为已触摸（用于显示验证错误）
  private markFormGroupTouched() {
    Object.values(this.tenantForm.controls).forEach((control) => {
      control.markAsTouched();
      // 如果是嵌套的表单组，递归处理
      if (control instanceof FormControl) {
        // 不需要特殊处理
      }
    });
  }

  // 比较两个值，返回较小的一个
  getMinDate(value1: number, value2: number): number {
    return Math.min(value1, value2);
  }
}

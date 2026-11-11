import { Component } from '@angular/core';
import { DataTableColumn, DataTableComponent, DataTableRow } from '../../plugins/data-table';

@Component({
  selector: 'app-data-table-demo',
  imports: [DataTableComponent],
  template: `
    <div class="page-header d-print-none">
      <div class="row align-items-center">
        <div class="col">
          <h2 class="page-title">Data Table Example</h2>
        </div>
      </div>

      <div class="page-body">
        <div class="container-xl">
          <div class="row">
            <div class="col-12">
              <tabler-data-table
                [title]="'User List'"
                [data]="users"
                [columns]="columns"
                [pageSize]="5"
                [showPagination]="true"
              />
            </div>
          </div>

          <div class="row mt-4">
            <div class="col-12">
              <h3>Table with custom templates</h3>
              <tabler-data-table
                [title]="'Product List'"
                [data]="products"
                [columns]="productColumns"
                [pageSize]="5"
                [showPagination]="true"
              />
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
})
export class DataTableDemoComponent {
  users: DataTableRow[] = [
    {
      id: 1,
      name: 'Zhang San',
      email: 'zhangsan@example.com',
      role: 'Admin',
      status: 'Active',
      createdAt: new Date('2023-01-15'),
    },
    {
      id: 2,
      name: 'Li Si',
      email: 'lisi@example.com',
      role: 'Editor',
      status: 'Active',
      createdAt: new Date('2023-02-20'),
    },
    {
      id: 3,
      name: 'Wang Wu',
      email: 'wangwu@example.com',
      role: 'User',
      status: 'Inactive',
      createdAt: new Date('2023-03-10'),
    },
    {
      id: 4,
      name: 'Zhao Liu',
      email: 'zhaoliu@example.com',
      role: 'Editor',
      status: 'Active',
      createdAt: new Date('2023-04-05'),
    },
    {
      id: 5,
      name: 'Qian Qi',
      email: 'qianqi@example.com',
      role: 'User',
      status: 'Active',
      createdAt: new Date('2023-05-12'),
    },
    {
      id: 6,
      name: 'Sun Ba',
      email: 'sunba@example.com',
      role: 'Admin',
      status: 'Inactive',
      createdAt: new Date('2023-06-18'),
    },
  ];

  columns: DataTableColumn[] = [
    { key: 'id', title: 'ID', sortable: true, width: '5rem' },
    { key: 'name', title: 'Name', sortable: true },
    { key: 'email', title: 'Email', sortable: true },
    { key: 'role', title: 'Role', sortable: true },
    {
      key: 'status',
      title: 'Status',
      sortable: true,
      cellTemplate: (value: unknown) => {
        const v = value as string;
        return `<span class="badge ${v === 'Active' ? 'bg-success' : 'bg-secondary'}">${v}</span>`;
      },
    },
    {
      key: 'createdAt',
      title: 'Created Date',
      sortable: true,
      cellTemplate: (value: unknown) => (value as Date).toLocaleDateString('zh-CN'),
    },
  ];

  products: DataTableRow[] = [
    {
      id: 1,
      name: 'Laptop',
      category: 'Electronics',
      price: 5999,
      stock: 50,
      status: 'In Stock',
    },
    {
      id: 2,
      name: 'Wireless Mouse',
      category: 'Accessories',
      price: 99,
      stock: 200,
      status: 'In Stock',
    },
    {
      id: 3,
      name: 'Mechanical Keyboard',
      category: 'Accessories',
      price: 399,
      stock: 0,
      status: 'Out of Stock',
    },
    {
      id: 4,
      name: 'Monitor',
      category: 'Electronics',
      price: 1299,
      stock: 30,
      status: 'In Stock',
    },
    {
      id: 5,
      name: 'USB-C Cable',
      category: 'Accessories',
      price: 29,
      stock: 500,
      status: 'In Stock',
    },
  ];

  productColumns: DataTableColumn[] = [
    { key: 'id', title: 'ID', sortable: true, width: '5rem' },
    { key: 'name', title: 'Product Name', sortable: true },
    { key: 'category', title: 'Category', sortable: true },
    {
      key: 'price',
      title: 'Price',
      sortable: true,
      cellTemplate: (value: unknown) => `¥${(value as number).toFixed(2)}`,
    },
    { key: 'stock', title: 'Stock', sortable: true },
    {
      key: 'status',
      title: 'Status',
      sortable: true,
      cellTemplate: (value: unknown) => {
        const v = value as string;
        return `<span class="badge ${v === 'In Stock' ? 'bg-success' : 'bg-danger'}">${v}</span>`;
      },
    },
  ];
}

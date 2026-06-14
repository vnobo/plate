import { Component } from '@angular/core';
import { DataTableColumn, DataTableComponent, DataTableRow } from '../../plugins/data-table';

@Component({
  selector: 'app-data-table-demo',
  imports: [DataTableComponent],
  template: `
    <div class="page-header d-print-none">
      <div class="row align-items-center">
        <div class="col">
          <h2 class="page-title">数据表格示例</h2>
        </div>
      </div>

      <div class="page-body">
        <div class="container-xl">
          <div class="row">
            <div class="col-12">
              <tabler-data-table
                [title]="'用户列表'"
                [data]="users"
                [columns]="columns"
                [pageSize]="5"
                [showPagination]="true"
              />
            </div>
          </div>

          <div class="row mt-4">
            <div class="col-12">
              <h3>带自定义模板的表格</h3>
              <tabler-data-table
                [title]="'产品列表'"
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
      name: '张三',
      email: 'zhangsan@example.com',
      role: '管理员',
      status: '活跃',
      createdAt: new Date('2023-01-15'),
    },
    {
      id: 2,
      name: '李四',
      email: 'lisi@example.com',
      role: '编辑',
      status: '活跃',
      createdAt: new Date('2023-02-20'),
    },
    {
      id: 3,
      name: '王五',
      email: 'wangwu@example.com',
      role: '用户',
      status: '非活跃',
      createdAt: new Date('2023-03-10'),
    },
    {
      id: 4,
      name: '赵六',
      email: 'zhaoliu@example.com',
      role: '编辑',
      status: '活跃',
      createdAt: new Date('2023-04-05'),
    },
    {
      id: 5,
      name: '钱七',
      email: 'qianqi@example.com',
      role: '用户',
      status: '活跃',
      createdAt: new Date('2023-05-12'),
    },
    {
      id: 6,
      name: '孙八',
      email: 'sunba@example.com',
      role: '管理员',
      status: '非活跃',
      createdAt: new Date('2023-06-18'),
    },
  ];

  columns: DataTableColumn[] = [
    { key: 'id', title: 'ID', sortable: true, width: '80px' },
    { key: 'name', title: '姓名', sortable: true },
    { key: 'email', title: '邮箱', sortable: true },
    { key: 'role', title: '角色', sortable: true },
    {
      key: 'status',
      title: '状态',
      sortable: true,
      cellTemplate: (value: unknown) => {
        const v = value as string;
        return `<span class="badge ${v === '活跃' ? 'bg-success' : 'bg-secondary'}">${v}</span>`;
      },
    },
    {
      key: 'createdAt',
      title: '创建日期',
      sortable: true,
      cellTemplate: (value: unknown) => (value as Date).toLocaleDateString('zh-CN'),
    },
  ];

  products: DataTableRow[] = [
    {
      id: 1,
      name: '笔记本电脑',
      category: '电子产品',
      price: 5999,
      stock: 50,
      status: '在售',
    },
    {
      id: 2,
      name: '无线鼠标',
      category: '配件',
      price: 99,
      stock: 200,
      status: '在售',
    },
    {
      id: 3,
      name: '机械键盘',
      category: '配件',
      price: 399,
      stock: 0,
      status: '缺货',
    },
    {
      id: 4,
      name: '显示器',
      category: '电子产品',
      price: 1299,
      stock: 30,
      status: '在售',
    },
    {
      id: 5,
      name: 'USB-C 数据线',
      category: '配件',
      price: 29,
      stock: 500,
      status: '在售',
    },
  ];

  productColumns: DataTableColumn[] = [
    { key: 'id', title: 'ID', sortable: true, width: '80px' },
    { key: 'name', title: '产品名称', sortable: true },
    { key: 'category', title: '类别', sortable: true },
    {
      key: 'price',
      title: '价格',
      sortable: true,
      cellTemplate: (value: unknown) => `¥${(value as number).toFixed(2)}`,
    },
    { key: 'stock', title: '库存', sortable: true },
    {
      key: 'status',
      title: '状态',
      sortable: true,
      cellTemplate: (value: unknown) => {
        const v = value as string;
        return `<span class="badge ${v === '在售' ? 'bg-success' : 'bg-danger'}">${v}</span>`;
      },
    },
  ];
}

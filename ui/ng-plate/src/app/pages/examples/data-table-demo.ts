import { Component } from '@angular/core';
import { DataTableComponent, DataTableColumn, DataTableRow } from '../../plugins/data-table';

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
  // 用户数据
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
    {
      id: 7,
      name: '周九',
      email: 'zhoujiu@example.com',
      role: '用户',
      status: '活跃',
      createdAt: new Date('2023-07-22'),
    },
    {
      id: 8,
      name: '吴十',
      email: 'wushi@example.com',
      role: '编辑',
      status: '活跃',
      createdAt: new Date('2023-08-30'),
    },
  ];

  // 用户表格列定义
  columns: DataTableColumn[] = [
    { key: 'id', title: 'ID', sortable: true },
    { key: 'name', title: '姓名', sortable: true },
    { key: 'email', title: '邮箱', sortable: true },
    { key: 'role', title: '角色', sortable: true },
    { key: 'status', title: '状态', sortable: true },
    { key: 'createdAt', title: '创建日期', sortable: true },
  ];

  // 产品数据
  products: DataTableRow[] = [
    { id: 1, name: '笔记本电脑', category: '电子产品', price: 5999.0, stock: 15, status: '有库存' },
    { id: 2, name: '无线鼠标', category: '电子产品', price: 199.5, stock: 50, status: '有库存' },
    { id: 3, name: '机械键盘', category: '电子产品', price: 899.0, stock: 8, status: '低库存' },
    { id: 4, name: '显示器', category: '电子产品', price: 2499.0, stock: 0, status: '缺货' },
    { id: 5, name: '办公椅', category: '家具', price: 1299.0, stock: 3, status: '低库存' },
    { id: 6, name: '办公桌', category: '家具', price: 1899.0, stock: 7, status: '有库存' },
    { id: 7, name: '台灯', category: '家居', price: 159.9, stock: 20, status: '有库存' },
    { id: 8, name: '耳机', category: '电子产品', price: 699.0, stock: 12, status: '有库存' },
  ];

  // 产品表格列定义（包含自定义模板）
  productColumns: DataTableColumn[] = [
    { key: 'id', title: 'ID', sortable: true },
    { key: 'name', title: '产品名称', sortable: true },
    { key: 'category', title: '分类', sortable: true },
    {
      key: 'price',
      title: '价格',
      sortable: true,
      cellTemplate: (value: number) => `¥${value.toFixed(2)}`,
    },
    {
      key: 'stock',
      title: '库存',
      sortable: true,
      cellTemplate: (value: number) => {
        if (value === 0) {
          return `<span class="badge bg-danger">缺货</span>`;
        } else if (value < 5) {
          return `<span class="badge bg-warning">低库存</span>`;
        } else {
          return `<span class="badge bg-success">${value}</span>`;
        }
      },
    },
    {
      key: 'status',
      title: '状态',
      sortable: true,
      cellTemplate: (value: string) => {
        const statusClass =
          value === '缺货' ? 'bg-danger' : value === '低库存' ? 'bg-warning' : 'bg-success';
        return `<span class="badge ${statusClass}">${value}</span>`;
      },
    },
  ];
}

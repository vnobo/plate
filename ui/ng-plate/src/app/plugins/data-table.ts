import { Component, computed, effect, input, output, signal } from '@angular/core';

export interface DataTableColumn<T = Record<string, unknown>> {
  key: string;
  title: string;
  sortable?: boolean;
  width?: string;
  cellTemplate?: (value: unknown, row: T) => string;
  headerTemplate?: (column: DataTableColumn<T>) => string;
}

export interface DataTableRow {
  [key: string]: unknown;
}

export interface DataTablePageEvent {
  pageIndex: number;
  pageSize: number;
  length: number;
}

@Component({
  selector: 'tabler-data-table',
  imports: [],
  template: `
    <div class="card">
      <div class="card-header">
        <h3 class="card-title">{{ title() }}</h3>
        <div class="ms-auto">
          <div class="input-icon">
            <input
              type="text"
              class="form-control"
              placeholder="搜索..."
              [value]="filterTextValue()"
              (input)="onFilterChange($event)"
            />
            <span class="input-icon-addon">
              <svg
                xmlns="http://www.w3.org/2000/svg"
                class="icon"
                width="24"
                height="24"
                viewBox="0 0 24 24"
                stroke-width="2"
                stroke="currentColor"
                fill="none"
                stroke-linecap="round"
                stroke-linejoin="round"
              >
                <path stroke="none" d="M0 0h24v24H0z" fill="none"></path>
                <path d="M10 10m-7 0a7 7 0 1 0 14 0a7 7 0 1 0 -14 0"></path>
                <path d="M21 21l-6 -6"></path>
              </svg>
            </span>
          </div>
        </div>
      </div>
      <div class="table-responsive">
        <table class="table table-vcenter card-table">
          <thead>
            <tr>
              @for (column of visibleColumns(); track column.key) {
                <th
                  [style.width]="column.width"
                  [class.cursor-pointer]="column.sortable"
                  (click)="column.sortable && sort(column.key)"
                >
                  @if (column.headerTemplate) {
                    <span [innerHTML]="column.headerTemplate(column)"></span>
                  } @else {
                    <span>{{ column.title }}</span>
                  }
                  @if (column.sortable) {
                    <span class="ms-1">
                      @if (sortColumnValue() === column.key && sortDirectionValue() === 'asc') {
                        <svg
                          xmlns="http://www.w3.org/2000/svg"
                          class="icon icon-tabler icon-tabler-chevron-up"
                          width="16"
                          height="16"
                          viewBox="0 0 24 24"
                          stroke-width="2"
                          stroke="currentColor"
                          fill="none"
                          stroke-linecap="round"
                          stroke-linejoin="round"
                        >
                          <path stroke="none" d="M0 0h24v24H0z" fill="none"></path>
                          <path d="M6 15l6 -6l6 6"></path>
                        </svg>
                      } @else if (
                        sortColumnValue() === column.key && sortDirectionValue() === 'desc'
                      ) {
                        <svg
                          xmlns="http://www.w3.org/2000/svg"
                          class="icon icon-tabler icon-tabler-chevron-down"
                          width="16"
                          height="16"
                          viewBox="0 0 24 24"
                          stroke-width="2"
                          stroke="currentColor"
                          fill="none"
                          stroke-linecap="round"
                          stroke-linejoin="round"
                        >
                          <path stroke="none" d="M0 0h24v24H0z" fill="none"></path>
                          <path d="M6 9l6 6l6 -6"></path>
                        </svg>
                      } @else {
                        <svg
                          xmlns="http://www.w3.org/2000/svg"
                          class="icon icon-tabler icon-tabler-arrows-sort"
                          width="16"
                          height="16"
                          viewBox="0 0 24 24"
                          stroke-width="2"
                          stroke="currentColor"
                          fill="none"
                          stroke-linecap="round"
                          stroke-linejoin="round"
                        >
                          <path stroke="none" d="M0 0h24v24H0z" fill="none"></path>
                          <path d="M3 9l4 -4l4 4m-4 -4v14"></path>
                          <path d="M21 15l-4 4l-4 -4m4 4v-14"></path>
                        </svg>
                      }
                    </span>
                  }
                </th>
              }
            </tr>
          </thead>
          <tbody>
            @for (row of paginatedData(); track row; let i = $index) {
              <tr>
                @for (column of visibleColumns(); track column.key) {
                  <td>
                    @if (column.cellTemplate) {
                      <span [innerHTML]="column.cellTemplate(row[column.key], row)"></span>
                    } @else {
                      <span>{{ row[column.key] }}</span>
                    }
                  </td>
                }
              </tr>
            } @empty {
              <tr>
                <td [attr.colspan]="columns().length" class="text-center py-4">没有找到数据</td>
              </tr>
            }
          </tbody>
        </table>
      </div>

      @if (showPagination()) {
        <div class="card-footer d-flex align-items-center">
          <div class="text-muted">
            显示第 {{ currentPageIndexValue() * pageSize() + 1 }} -
            {{ min((currentPageIndexValue() + 1) * pageSize(), filteredData().length) }} 条，共
            {{ filteredData().length }} 条
          </div>
          <div class="ms-auto">
            <ul class="pagination mb-0">
              <li class="page-item" [class.disabled]="currentPageIndexValue() === 0">
                <a
                  class="page-link"
                  href="javascript:void(0)"
                  (click)="goToPage(currentPageIndexValue() - 1)"
                  aria-label="Previous"
                >
                  <svg
                    xmlns="http://www.w3.org/2000/svg"
                    class="icon"
                    width="24"
                    height="24"
                    viewBox="0 0 24 24"
                    stroke-width="2"
                    stroke="currentColor"
                    fill="none"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                  >
                    <path stroke="none" d="M0 0h24v24H0z" fill="none"></path>
                    <path d="M15 6l-6 6l6 6"></path>
                  </svg>
                </a>
              </li>

              @for (page of pages(); track $index) {
                <li class="page-item" [class.active]="page === currentPageIndexValue()">
                  <a class="page-link" href="javascript:void(0)" (click)="goToPage(page)">{{
                    page + 1
                  }}</a>
                </li>
              }

              <li class="page-item" [class.disabled]="currentPageIndexValue() === totalPages() - 1">
                <a
                  class="page-link"
                  href="javascript:void(0)"
                  (click)="goToPage(currentPageIndexValue() + 1)"
                  aria-label="Next"
                >
                  <svg
                    xmlns="http://www.w3.org/2000/svg"
                    class="icon"
                    width="24"
                    height="24"
                    viewBox="0 0 24 24"
                    stroke-width="2"
                    stroke="currentColor"
                    fill="none"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                  >
                    <path stroke="none" d="M0 0h24v24H0z" fill="none"></path>
                    <path d="M9 6l6 6l-6 6"></path>
                  </svg>
                </a>
              </li>
            </ul>
          </div>
        </div>
      }
    </div>
  `,
  styles: `
    .cursor-pointer {
      cursor: pointer;
    }

    .card {
      border: 1px solid var(--tblr-border-color);
      border-radius: var(--tblr-border-radius);
      background: var(--tblr-bg-surface);
      box-shadow: var(--tblr-shadow-card);
    }

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 0.75rem 1rem;
      border-bottom: 1px solid var(--tblr-border-color);
      background: var(--tblr-bg-surface-secondary);
      border-top-left-radius: calc(var(--tblr-border-radius) - 1px);
      border-top-right-radius: calc(var(--tblr-border-radius) - 1px);
    }

    .card-title {
      margin: 0;
      font-size: 1rem;
      font-weight: 500;
    }

    .table-responsive {
      overflow-x: auto;
    }

    .table {
      margin-bottom: 0;
    }

    .table th {
      border-top: none;
      font-weight: 600;
      font-size: 0.875rem;
      text-transform: uppercase;
      letter-spacing: 0.02em;
    }

    .table td {
      vertical-align: middle;
    }

    .card-footer {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 0.75rem 1rem;
      border-top: 1px solid var(--tblr-border-color);
      background: var(--tblr-bg-surface-secondary);
      border-bottom-left-radius: calc(var(--tblr-border-radius) - 1px);
      border-bottom-right-radius: calc(var(--tblr-border-radius) - 1px);
    }

    .pagination {
      margin: 0;
    }

    .page-item.active .page-link {
      background: var(--tblr-primary);
      border-color: var(--tblr-primary);
      color: var(--tblr-white);
    }

    .page-link {
      color: var(--tblr-text-secondary);
      text-decoration: none;
    }

    .input-icon {
      position: relative;
    }

    .input-icon-addon {
      position: absolute;
      left: 0;
      top: 0;
      bottom: 0;
      display: flex;
      align-items: center;
      justify-content: center;
      width: 2rem;
      color: var(--tblr-icon-color);
      pointer-events: none;
    }

    .form-control {
      padding-left: 2rem;
    }
  `,
})
export class DataTableComponent {
  title = input<string>('');
  data = input.required<DataTableRow[]>();
  columns = input.required<DataTableColumn[]>();
  pageSize = input<number>(10);
  showPagination = input<boolean>(true);

  pageChange = output<DataTablePageEvent>();

  protected readonly filterTextValue = signal<string>('');
  protected readonly sortColumnValue = signal<string | null>(null);
  protected readonly sortDirectionValue = signal<'asc' | 'desc'>('asc');
  protected readonly currentPageIndexValue = signal<number>(0);

  readonly filteredData = computed(() => {
    const data = this.data();
    const filterText = this.filterTextValue().toLowerCase();

    if (!filterText) {
      return data;
    }

    return data.filter((row) => {
      return Object.values(row).some((value) => String(value).toLowerCase().includes(filterText));
    });
  });

  readonly sortedData = computed(() => {
    const data = this.filteredData();
    const column = this.sortColumnValue();
    const direction = this.sortDirectionValue();

    if (!column) {
      return data;
    }

    return [...data].sort((a, b) => {
      const aVal = a[column];
      const bVal = b[column];

      if (aVal == null && bVal == null) return 0;
      if (aVal == null) return direction === 'asc' ? 1 : -1;
      if (bVal == null) return direction === 'asc' ? -1 : 1;

      if (typeof aVal === 'number' && typeof bVal === 'number') {
        return direction === 'asc' ? aVal - bVal : bVal - aVal;
      }

      if (aVal instanceof Date && bVal instanceof Date) {
        return direction === 'asc'
          ? aVal.getTime() - bVal.getTime()
          : bVal.getTime() - aVal.getTime();
      }

      const aStr = String(aVal).toLowerCase();
      const bStr = String(bVal).toLowerCase();

      if (aStr < bStr) return direction === 'asc' ? -1 : 1;
      if (aStr > bStr) return direction === 'asc' ? 1 : -1;
      return 0;
    });
  });

  readonly paginatedData = computed(() => {
    if (!this.showPagination()) {
      return this.sortedData();
    }

    const data = this.sortedData();
    const pageIndex = this.currentPageIndexValue();
    const pageSize = this.pageSize();

    const startIndex = pageIndex * pageSize;
    return data.slice(startIndex, startIndex + pageSize);
  });

  readonly totalPages = computed(() => {
    if (!this.showPagination()) {
      return 1;
    }

    const totalItems = this.filteredData().length;
    const pageSize = this.pageSize();
    return Math.ceil(totalItems / pageSize);
  });

  readonly pages = computed(() => {
    const totalPages = this.totalPages();
    const currentPage = this.currentPageIndexValue();

    const maxVisiblePages = 5;
    let startPage = Math.max(0, currentPage - Math.floor(maxVisiblePages / 2));
    let endPage = Math.min(totalPages - 1, startPage + maxVisiblePages - 1);

    if (endPage - startPage < maxVisiblePages - 1) {
      startPage = Math.max(0, endPage - maxVisiblePages + 1);
    }

    const pages = [];
    for (let i = startPage; i <= endPage; i++) {
      pages.push(i);
    }

    return pages;
  });

  readonly visibleColumns = computed(() => {
    return this.columns().filter((col) => col.key !== 'actions');
  });

  constructor() {
    effect(() => {
      this.data();
      this.filterTextValue.set('');
      this.currentPageIndexValue.set(0);
    });
  }

  protected onFilterChange(event: Event) {
    const target = event.target as HTMLInputElement;
    this.filterTextValue.set(target.value);
    this.currentPageIndexValue.set(0);
  }

  protected sort(columnKey: string) {
    if (this.sortColumnValue() === columnKey) {
      this.sortDirectionValue.set(this.sortDirectionValue() === 'asc' ? 'desc' : 'asc');
    } else {
      this.sortColumnValue.set(columnKey);
      this.sortDirectionValue.set('asc');
    }
    this.currentPageIndexValue.set(0);
  }

  protected goToPage(pageIndex: number) {
    const totalPages = this.totalPages();

    if (pageIndex < 0 || pageIndex >= totalPages) {
      return;
    }

    this.currentPageIndexValue.set(pageIndex);

    this.pageChange.emit({
      pageIndex,
      pageSize: this.pageSize(),
      length: this.filteredData().length,
    });
  }

  protected min(a: number, b: number): number {
    return Math.min(a, b);
  }
}

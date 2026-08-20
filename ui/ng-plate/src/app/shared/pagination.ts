import { Component, computed, input, output } from '@angular/core';

/**
 * Shared pagination bar used by all platform list pages.
 *
 * Renders a summary line, a page-size selector and a page navigation with
 * ellipsis markers. Parent components place it inside a card footer and react
 * to `pageChange` / `sizeChange` to update their pageable signal.
 */
@Component({
  selector: 'app-pagination',
  template: `
    <div class="d-flex align-items-center flex-wrap gap-2">
      <p class="m-0 text-secondary">
        显示 {{ start() }} 到 {{ end() }} 项，共 {{ totalElements() }} 项
      </p>
      <div class="ms-auto d-flex align-items-center flex-wrap gap-2">
        <select
          class="form-select form-select-sm w-auto"
          [value]="size()"
          (change)="onSizeChange($event)"
          aria-label="每页显示条数"
        >
          @for (s of PAGE_SIZES; track s) {
            <option [value]="s">每页 {{ s }} 条</option>
          }
        </select>
        <nav aria-label="分页导航">
          <ul class="pagination pagination-sm m-0">
            <li class="page-item" [class.disabled]="page() <= 1">
              <button
                type="button"
                class="page-link"
                [disabled]="page() <= 1"
                (click)="goto(page() - 1)"
                aria-label="上一页"
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
              </button>
            </li>
            @for (p of pages(); track $index) {
              @if (p === -1) {
                <li class="page-item disabled" aria-hidden="true">
                  <span class="page-link">…</span>
                </li>
              } @else {
                <li class="page-item" [class.active]="p === page()">
                  <button
                    type="button"
                    class="page-link"
                    (click)="goto(p)"
                    [attr.aria-current]="p === page() ? 'page' : null"
                    [attr.aria-label]="'第 ' + p + ' 页'"
                  >
                    {{ p }}
                  </button>
                </li>
              }
            }
            <li class="page-item" [class.disabled]="page() >= totalPages()">
              <button
                type="button"
                class="page-link"
                [disabled]="page() >= totalPages()"
                (click)="goto(page() + 1)"
                aria-label="下一页"
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
              </button>
            </li>
          </ul>
        </nav>
      </div>
    </div>
  `,
})
export class Pagination {
  /** Current page, 1-based. */
  readonly page = input.required<number>();
  /** Page size. */
  readonly size = input.required<number>();
  /** Total number of rows across all pages. */
  readonly totalElements = input.required<number>();

  readonly pageChange = output<number>();
  readonly sizeChange = output<number>();

  protected readonly PAGE_SIZES = [10, 20, 50];

  protected readonly totalPages = computed(() =>
    Math.max(1, Math.ceil(this.totalElements() / this.size())),
  );

  protected readonly start = computed(() =>
    this.totalElements() === 0 ? 0 : (this.page() - 1) * this.size() + 1,
  );

  protected readonly end = computed(() =>
    Math.min(this.page() * this.size(), this.totalElements()),
  );

  /**
   * Visible page numbers. `-1` marks an ellipsis gap.
   * Always shows the first page, the last page and a window around the
   * current page.
   */
  protected readonly pages = computed<number[]>(() => {
    const totalPages = this.totalPages();
    const currentPage = this.page();
    const result: number[] = [];

    if (totalPages >= 1) {
      result.push(1);
    }
    if (currentPage > 3) {
      result.push(-1);
    }
    for (
      let i = Math.max(2, currentPage - 1);
      i <= Math.min(totalPages - 1, currentPage + 1);
      i++
    ) {
      result.push(i);
    }
    if (currentPage < totalPages - 2) {
      result.push(-1);
    }
    if (totalPages > 1) {
      result.push(totalPages);
    }
    return result;
  });

  protected goto(page: number): void {
    if (page < 1 || page > this.totalPages() || page === this.page()) {
      return;
    }
    this.pageChange.emit(page);
  }

  protected onSizeChange(event: Event): void {
    const value = Number((event.target as HTMLSelectElement).value);
    if (value > 0 && value !== this.size()) {
      this.sizeChange.emit(value);
    }
  }
}

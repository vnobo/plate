import { computed, inject, Service, signal } from '@angular/core';
import { DOCUMENT } from '@angular/common';

@Service()
export class LayoutService {
  private readonly document = inject(DOCUMENT);
  private readonly _collapsed = signal(false);

  readonly collapsed = this._collapsed.asReadonly();
  readonly expanded = computed(() => !this._collapsed());

  toggleCollapsed(): void {
    this._collapsed.update((value) => {
      const next = !value;
      this.document.body.classList.toggle('layout-collapsed', next);
      return next;
    });
  }

  setCollapsed(value: boolean): void {
    this._collapsed.set(value);
    this.document.body.classList.toggle('layout-collapsed', value);
  }
}

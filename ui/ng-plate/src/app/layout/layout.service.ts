import { computed, Service, signal } from '@angular/core';

@Service()
export class LayoutService {
  private readonly _collapsed = signal(false);
  private readonly _mobileOpen = signal(false);

  readonly collapsed = this._collapsed.asReadonly();
  readonly mobileOpen = this._mobileOpen.asReadonly();
  readonly expanded = computed(() => !this._collapsed());

  toggleCollapsed(): void {
    this._collapsed.update((value) => !value);
  }

  toggleMobile(): void {
    this._mobileOpen.update((value) => !value);
  }

  closeMobile(): void {
    this._mobileOpen.set(false);
  }
}

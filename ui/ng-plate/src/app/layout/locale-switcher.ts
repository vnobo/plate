import { Component, computed, inject, input } from '@angular/core';

import { LocaleService, SUPPORTED_LOCALES, type LocaleId } from '@app/core';

/**
 * Language menu shared by the admin layout, the auth pages, the error page and the theme
 * gallery. Switching reloads the page in the other localized build (see LocaleService).
 */
@Component({
  selector: 'app-locale-switcher',
  host: {
    '[class]': 'hostClass()',
  },
  template: `
    <a
      href="javascript:void(0)"
      [class]="triggerClass()"
      data-bs-toggle="dropdown"
      i18n-aria-label="@@localeSwitchLabel"
      aria-label="切换语言"
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
        aria-hidden="true"
      >
        <path stroke="none" d="M0 0h24v24H0z" fill="none" />
        <path d="M3 12a9 9 0 1 0 18 0a9 9 0 0 0 -18 0" />
        <path d="M3.6 9h16.8" />
        <path d="M3.6 15h16.8" />
        <path d="M11.5 3a17 17 0 0 0 0 18" />
        <path d="M12.5 3a17 17 0 0 1 0 18" />
      </svg>
      <span [class]="labelClass()">{{ locale.currentLabel() }}</span>
    </a>
    <div class="dropdown-menu dropdown-menu-end">
      @for (item of locales; track item.id) {
        <button
          class="dropdown-item d-flex align-items-center"
          type="button"
          [attr.lang]="item.id"
          [class.active]="item.id === locale.current()"
          [attr.aria-current]="item.id === locale.current() ? 'true' : null"
          (click)="select(item.id)"
        >
          <span class="flex-fill">{{ item.label }}</span>
          @if (item.id === locale.current()) {
            <svg
              xmlns="http://www.w3.org/2000/svg"
              class="icon ms-2 text-primary"
              width="24"
              height="24"
              viewBox="0 0 24 24"
              stroke-width="2"
              stroke="currentColor"
              fill="none"
              stroke-linecap="round"
              stroke-linejoin="round"
              aria-hidden="true"
            >
              <path stroke="none" d="M0 0h24v24H0z" fill="none" />
              <path d="M5 12l5 5L20 7" />
            </svg>
          }
        </button>
      }
    </div>
  `,
})
export class LocaleSwitcher {
  protected readonly locale = inject(LocaleService);
  protected readonly locales = SUPPORTED_LOCALES;

  /** `nav` fits inside `.navbar-nav`; `ghost` is a standalone icon button. */
  readonly variant = input<'nav' | 'ghost'>('nav');

  protected readonly hostClass = computed(() =>
    this.variant() === 'nav' ? 'nav-item dropdown' : 'dropdown',
  );

  protected readonly triggerClass = computed(() =>
    this.variant() === 'nav'
      ? 'nav-link px-2 d-flex align-items-center'
      : 'btn btn-ghost-secondary btn-icon',
  );

  protected readonly labelClass = computed(() =>
    this.variant() === 'nav' ? 'ms-1' : 'visually-hidden',
  );

  protected select(locale: LocaleId): void {
    this.locale.switch(locale);
  }
}

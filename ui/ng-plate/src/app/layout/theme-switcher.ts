import { Component, computed, inject, input } from '@angular/core';

import { PLATE_THEMES, SettingsService } from '@app/core';

/**
 * Season palette menu shared by the admin header and the passport (login) page.
 * Switching only changes the season axis; the light/dark form is left untouched.
 */
@Component({
  selector: 'app-theme-switcher',
  host: {
    '[class]': 'hostClass()',
  },
  template: `
    <a
      href="javascript:void(0)"
      [class]="triggerClass()"
      data-bs-toggle="dropdown"
      i18n-aria-label="@@themeSwitcherLabel"
      aria-label="切换主题"
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
        <circle cx="12" cy="12" r="9" />
        <circle cx="8" cy="10" r="1" />
        <circle cx="12" cy="7.5" r="1" />
        <circle cx="16" cy="10" r="1" />
        <path d="M8 16h8" />
      </svg>
      <span [class]="labelClass()">{{ currentTheme().name }}</span>
    </a>
    <div class="dropdown-menu dropdown-menu-end dropdown-menu-arrow">
      @for (theme of themes; track theme.id) {
        <button
          class="dropdown-item d-flex align-items-center"
          type="button"
          [class.active]="theme.id === currentTheme().id"
          [attr.aria-current]="theme.id === currentTheme().id ? 'true' : null"
          (click)="select(theme.id)"
        >
          <span class="avatar avatar-xs me-2" [style.background]="theme.swatch"></span>
          <span class="flex-fill">
            <span class="d-block">{{ theme.name }}</span>
            <span class="d-block small text-secondary">{{ theme.description }}</span>
          </span>
          @if (theme.id === currentTheme().id) {
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
export class ThemeSwitcher {
  protected readonly settings = inject(SettingsService);
  protected readonly themes = PLATE_THEMES;

  protected readonly currentTheme = computed(
    () => PLATE_THEMES.find((theme) => theme.id === this.settings.palette()) ?? PLATE_THEMES[0]!,
  );

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
    this.variant() === 'nav' ? 'd-none d-xl-inline ms-1' : 'visually-hidden',
  );

  protected select(id: string): void {
    this.settings.setPalette(id);
  }
}

import { DOCUMENT, isPlatformBrowser } from '@angular/common';
import {
  LOCALE_ID,
  PLATFORM_ID,
  Service,
  computed,
  inject,
  signal,
} from '@angular/core';

import { BrowserStorage } from '../storage/browser-storage';
import {
  DEFAULT_LOCALE,
  LOCALE_COOKIE_NAME,
  LOCALE_STORAGE_KEY,
  SUPPORTED_LOCALES,
  localeById,
  parseLocale,
  parseLocaleFromPath,
  switchLocaleUrl,
  type LocaleId,
} from './locale';

const COOKIE_MAX_AGE_SECONDS = 60 * 60 * 24 * 365;

/**
 * Single source of truth for the active locale.
 *
 * The localized build is the authority: every build stamps `<html lang>` with its own
 * locale, so the markup decides which language is running. The user choice is only
 * persisted so the server can redirect `/` next time; switching reloads the page because
 * @angular/localize resolves translations at build time.
 */
@Service()
export class LocaleService {
  private readonly _document = inject(DOCUMENT);
  private readonly _platformId = inject(PLATFORM_ID);
  private readonly _storage = inject(BrowserStorage);
  private readonly _localeId = inject(LOCALE_ID);
  private readonly _isBrowser = isPlatformBrowser(this._platformId);

  readonly locales = SUPPORTED_LOCALES;
  readonly current = signal<LocaleId>(this.detect());
  readonly currentLabel = computed(() => localeById(this.current()).label);

  /**
   * Reloads the app in `target`, keeping the current path, query and hash.
   * No-op on the server and when `target` is already active.
   */
  switch(target: LocaleId): void {
    if (!this._isBrowser || target === this.current()) return;
    this.persist(target);

    const location = this._document.location;
    if (!location) return;
    const next = switchLocaleUrl(target, location.href);
    if (next !== `${location.pathname}${location.search}${location.hash}`) {
      location.assign(next);
    }
  }

  /** Build locale from the served markup, with URL prefix and LOCALE_ID as fallbacks. */
  private detect(): LocaleId {
    const fromMarkup = parseLocale(this._document.documentElement?.lang);
    if (fromMarkup) return fromMarkup;

    if (this._isBrowser) {
      const fromPath = parseLocaleFromPath(this._document.location?.pathname ?? '');
      if (fromPath) return fromPath;
    }

    return parseLocale(this._localeId) ?? DEFAULT_LOCALE;
  }

  /** The cookie is the server-visible copy; storage keeps the choice client-side too. */
  private persist(locale: LocaleId): void {
    this._storage.setItem(LOCALE_STORAGE_KEY, locale);
    this._document.cookie =
      `${LOCALE_COOKIE_NAME}=${locale}; path=/; max-age=${COOKIE_MAX_AGE_SECONDS}; SameSite=Lax`;
  }
}

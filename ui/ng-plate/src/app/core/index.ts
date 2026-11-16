export { indexInterceptor } from './net/http.Interceptor';
export { SessionStorage } from './storage/session-storage';
export { BrowserStorage } from './storage/browser-storage';

export {
  DEFAULT_LOCALE,
  LOCALE_COOKIE_NAME,
  LOCALE_STORAGE_KEY,
  SUPPORTED_LOCALES,
  localeById,
  localeFromAcceptLanguage,
  localeFromCookie,
  localeFromPath,
  localePrefix,
  normalizeLocale,
  parseLocale,
  parseLocaleFromPath,
  stripLocalePrefix,
  switchLocaleUrl,
} from './i18n/locale';
export type { LocaleId, SupportedLocale } from './i18n/locale';
export { LocaleService } from './i18n/locale.service';

export { TokenService } from './services/token.service';
export { UserService } from './services/user.service';
export { PLATE_THEMES, SettingsService } from './services/settings.service';
export type { PlateTheme } from './services/settings.service';
export { canActivateGuard, roleChildGuard } from './pages.guard';

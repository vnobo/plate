import dayjs from 'dayjs';
import 'dayjs/locale/zh-cn';

/**
 * Angular locale id -> dayjs locale. `en` is dayjs' built-in locale; only `zh-cn` needs an
 * import. Keep this map in sync with `SUPPORTED_LOCALES` in `@app/core`.
 */
const DAYJS_LOCALES: Readonly<Record<string, string>> = {
  'zh-Hans': 'zh-cn',
  'en-US': 'en',
};

/**
 * dayjs keeps one global locale, so point it at the language the page was built for.
 * Localized builds stamp `<html lang>` with their locale; before that attribute exists
 * (or outside a DOM) the source locale is the safe default.
 */
export function applyDayjsLocale(localeId: string | null | undefined): void {
  dayjs.locale(DAYJS_LOCALES[localeId?.trim() ?? ''] ?? DAYJS_LOCALES['zh-Hans']);
}

if (typeof document !== 'undefined' && document.documentElement.lang) {
  applyDayjsLocale(document.documentElement.lang);
}

export default dayjs;

export { dayjs as Dayjs };

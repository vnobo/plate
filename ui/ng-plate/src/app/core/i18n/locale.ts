/**
 * Locale registry and URL helpers shared by the Angular runtime, the Express server and
 * unit tests. This module is intentionally free of Angular imports so the SSR server
 * bundle (src/server.ts) can reuse the exact same rules without pulling in the framework.
 */

export type LocaleId = 'zh-Hans' | 'en-US';

export interface SupportedLocale {
  readonly id: LocaleId;
  /** Endonym shown in the language menu; deliberately not translated. */
  readonly label: string;
  /** URL segment used by the Angular i18n `subPath`; empty for the source locale. */
  readonly subPath: string;
}

/** Source locale, served from `/`. */
export const DEFAULT_LOCALE: LocaleId = 'zh-Hans';

export const SUPPORTED_LOCALES: readonly SupportedLocale[] = [
  { id: 'zh-Hans', label: '简体中文', subPath: '' },
  { id: 'en-US', label: 'English', subPath: 'en-US' },
];

/** Explicit language choice shared with the server; both keys keep the same name on purpose. */
export const LOCALE_COOKIE_NAME = 'plate-lang';
export const LOCALE_STORAGE_KEY = 'plate-lang';

/** Values written by older builds, before the canonical locale ids landed. */
const LEGACY_ALIASES: Readonly<Record<string, LocaleId>> = {
  en: 'en-US',
  'en-us': 'en-US',
  'en_us': 'en-US',
  zh: 'zh-Hans',
  'zh-cn': 'zh-Hans',
  'zh_cn': 'zh-Hans',
};

/** Returns the canonical id for `value`, or `null` when the value is unknown. */
export function parseLocale(value: string | null | undefined): LocaleId | null {
  const candidate = value?.trim().toLowerCase();
  if (!candidate) return null;
  const supported = SUPPORTED_LOCALES.find((locale) => locale.id.toLowerCase() === candidate);
  if (supported) return supported.id;
  return LEGACY_ALIASES[candidate] ?? null;
}

/** Like `parseLocale`, but falls back to the source locale instead of returning `null`. */
export function normalizeLocale(value: string | null | undefined): LocaleId {
  return parseLocale(value) ?? DEFAULT_LOCALE;
}

export function localeById(id: LocaleId): SupportedLocale {
  return SUPPORTED_LOCALES.find((locale) => locale.id === id) ?? SUPPORTED_LOCALES[0]!;
}

/** URL segment for `id`, e.g. `''` for zh-Hans and `'/en-US'` for en-US. */
export function localePrefix(id: LocaleId): string {
  const { subPath } = localeById(id);
  return subPath ? `/${subPath}` : '';
}

/** Locale encoded in `pathname`, or `null` when the path carries no locale prefix. */
export function parseLocaleFromPath(pathname: string): LocaleId | null {
  for (const locale of SUPPORTED_LOCALES) {
    if (!locale.subPath) continue;
    const prefix = `/${locale.subPath}`;
    if (pathname === prefix || pathname.startsWith(`${prefix}/`)) return locale.id;
  }
  return null;
}

/** Locale encoded in `pathname`, falling back to the source locale. */
export function localeFromPath(pathname: string): LocaleId {
  return parseLocaleFromPath(pathname) ?? DEFAULT_LOCALE;
}

/** Removes any supported locale prefix from `pathname`. */
export function stripLocalePrefix(pathname: string): string {
  for (const locale of SUPPORTED_LOCALES) {
    if (!locale.subPath) continue;
    const prefix = `/${locale.subPath}`;
    if (pathname === prefix) return '/';
    if (pathname.startsWith(`${prefix}/`)) return pathname.slice(prefix.length);
  }
  return pathname;
}

/**
 * Absolute URL for the same page in `target`, preserving query and hash.
 * A switch from `/` to en-US yields `/en-US/` so the localized `base href` matches.
 */
export function switchLocaleUrl(target: LocaleId, href: string): string {
  const url = new URL(href);
  const path = stripLocalePrefix(url.pathname);
  const prefix = localePrefix(target);
  const nextPath = prefix ? (path === '/' ? `${prefix}/` : `${prefix}${path}`) : path;
  return `${nextPath}${url.search}${url.hash}`;
}

/** Locale from a `plate-lang` cookie header, or `null` when absent/unknown. */
export function localeFromCookie(cookieHeader: string | null | undefined): LocaleId | null {
  if (!cookieHeader) return null;
  const match = new RegExp(`(?:^|;\\s*)${LOCALE_COOKIE_NAME}=([^;]*)`).exec(cookieHeader);
  if (!match?.[1]) return null;
  try {
    return parseLocale(decodeURIComponent(match[1]));
  } catch {
    return null;
  }
}

/** Locale preferred by an `Accept-Language` header, or `null` when it lists neither locale. */
export function localeFromAcceptLanguage(
  header: string | null | undefined,
): LocaleId | null {
  if (!header) return null;
  const candidates = header
    .split(',')
    .map((part, index) => {
      const [tag = '', ...params] = part.trim().split(';');
      const quality = params.find((param) => param.trim().startsWith('q='));
      const weight = quality ? Number.parseFloat(quality.trim().slice(2)) : 1;
      return { tag: tag.trim().toLowerCase(), weight: Number.isFinite(weight) ? weight : 0, index };
    })
    .filter((candidate) => candidate.tag !== '' && candidate.weight > 0)
    .sort((a, b) => b.weight - a.weight || a.index - b.index);

  for (const { tag } of candidates) {
    if (tag === 'en' || tag.startsWith('en-')) return 'en-US';
    if (tag === 'zh' || tag.startsWith('zh-')) return 'zh-Hans';
  }
  return null;
}

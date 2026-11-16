import {
  DEFAULT_LOCALE,
  localeFromAcceptLanguage,
  localeFromCookie,
  localeFromPath,
  localePrefix,
  normalizeLocale,
  parseLocale,
  parseLocaleFromPath,
  stripLocalePrefix,
  switchLocaleUrl,
} from './locale';

describe('locale registry', () => {
  it('parses canonical locale ids case-insensitively', () => {
    expect(parseLocale('en-US')).toBe('en-US');
    expect(parseLocale('EN-us')).toBe('en-US');
    expect(parseLocale(' zh-Hans ')).toBe('zh-Hans');
  });

  it('migrates the legacy values written before the canonical ids landed', () => {
    expect(parseLocale('en')).toBe('en-US');
    expect(parseLocale('en_US')).toBe('en-US');
    expect(parseLocale('zh')).toBe('zh-Hans');
    expect(parseLocale('zh-CN')).toBe('zh-Hans');
  });

  it('returns null for unknown or empty values and defaults in normalizeLocale', () => {
    expect(parseLocale('fr')).toBeNull();
    expect(parseLocale('')).toBeNull();
    expect(parseLocale(null)).toBeNull();
    expect(normalizeLocale('fr')).toBe(DEFAULT_LOCALE);
  });

  it('builds locale prefixes for the Angular i18n subPath', () => {
    expect(localePrefix('zh-Hans')).toBe('');
    expect(localePrefix('en-US')).toBe('/en-US');
  });

  it('detects a locale only on a segment boundary', () => {
    expect(parseLocaleFromPath('/')).toBeNull();
    expect(parseLocaleFromPath('/passport/login')).toBeNull();
    expect(parseLocaleFromPath('/en-US')).toBe('en-US');
    expect(parseLocaleFromPath('/en-US/')).toBe('en-US');
    expect(parseLocaleFromPath('/en-US/passport/login')).toBe('en-US');
    expect(parseLocaleFromPath('/en-USX')).toBeNull();
    expect(localeFromPath('/fr/x')).toBe(DEFAULT_LOCALE);
  });

  it('strips any supported locale prefix', () => {
    expect(stripLocalePrefix('/en-US')).toBe('/');
    expect(stripLocalePrefix('/en-US/')).toBe('/');
    expect(stripLocalePrefix('/en-US/a/b')).toBe('/a/b');
    expect(stripLocalePrefix('/a/b')).toBe('/a/b');
  });
});

describe('switchLocaleUrl', () => {
  it('adds the en-US prefix and keeps query and hash', () => {
    expect(switchLocaleUrl('en-US', 'http://localhost/passport/login?a=1#top')).toBe(
      '/en-US/passport/login?a=1#top',
    );
  });

  it('routes the root page to the localized root', () => {
    expect(switchLocaleUrl('en-US', 'http://localhost/')).toBe('/en-US/');
    expect(switchLocaleUrl('zh-Hans', 'http://localhost/en-US/')).toBe('/');
  });

  it('removes the prefix when switching back to the source locale', () => {
    expect(switchLocaleUrl('zh-Hans', 'http://localhost/en-US/passport/login?x=2')).toBe(
      '/passport/login?x=2',
    );
  });

  it('does not double the prefix', () => {
    expect(switchLocaleUrl('en-US', 'http://localhost/en-US/a')).toBe('/en-US/a');
  });

  it('treats a prefix without trailing slash as the root', () => {
    expect(switchLocaleUrl('zh-Hans', 'http://localhost/en-US')).toBe('/');
  });
});

describe('server locale negotiation', () => {
  it('reads the plate-lang cookie', () => {
    expect(localeFromCookie('plate-lang=en-US')).toBe('en-US');
    expect(localeFromCookie('a=1; plate-lang=zh-Hans; b=2')).toBe('zh-Hans');
    expect(localeFromCookie('plate-lang=en')).toBe('en-US');
    expect(localeFromCookie('plate-lang=fr')).toBeNull();
    expect(localeFromCookie(undefined)).toBeNull();
  });

  it('picks the highest weighted supported language from Accept-Language', () => {
    expect(localeFromAcceptLanguage('en-US,en;q=0.9')).toBe('en-US');
    expect(localeFromAcceptLanguage('zh-CN,zh;q=0.9,en;q=0.8')).toBe('zh-Hans');
    expect(localeFromAcceptLanguage('fr;q=1,en;q=0.5')).toBe('en-US');
    expect(localeFromAcceptLanguage('en;q=0,fr')).toBeNull();
    expect(localeFromAcceptLanguage('fr')).toBeNull();
    expect(localeFromAcceptLanguage('')).toBeNull();
    expect(localeFromAcceptLanguage(undefined)).toBeNull();
  });
});

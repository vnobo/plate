import {
  ApplicationConfig,
  importProvidersFrom,
  inject,
  LOCALE_ID,
  provideAppInitializer,
  provideExperimentalZonelessChangeDetection,
} from '@angular/core';
import {provideRouter, withComponentInputBinding} from '@angular/router';

import {
  provideHttpClient,
  withFetch,
  withInterceptors,
  withInterceptorsFromDi,
  withXsrfConfiguration
} from '@angular/common/http';
import {FormsModule} from '@angular/forms';
import {provideAnimationsAsync} from '@angular/platform-browser/animations/async';

import {NzConfig, provideNzConfig} from 'ng-zorro-antd/core/config';
import {provideNzI18n, zh_CN} from 'ng-zorro-antd/i18n';
import {provideNzIcons} from 'ng-zorro-antd/icon';

import {routes} from './app.routes';
import {indexInterceptor} from './core';
import {icons} from '../style-icons';
import {ThemeService} from './core/services/theme.service';

const ngZorroConfig: NzConfig = {
  message: { nzDuration: 2000, nzMaxStack: 3 },
  notification: {
    nzTop: '4.7rem',
    nzDuration: 1000 * 10,
    nzPlacement: 'bottomRight',
  },
};

export const appConfig: ApplicationConfig = {
  providers: [
    provideNzIcons(icons),
    provideNzI18n(zh_CN),
    provideNzConfig(ngZorroConfig),
    importProvidersFrom(FormsModule),
    provideAnimationsAsync(),
    provideHttpClient(
      withFetch(),
      withInterceptorsFromDi(),
      withInterceptors(indexInterceptor),
      withXsrfConfiguration({
        cookieName: 'XSRF-TOKEN',
        headerName: 'X-XSRF-TOKEN',
      }),
    ),
    { provide: LOCALE_ID, useValue: 'zh_CN' },
    provideAppInitializer(() => {
      const themeService = inject(ThemeService);
      return themeService.loadTheme();
    }),
    provideRouter(routes, withComponentInputBinding()),
    provideExperimentalZonelessChangeDetection(),
  ],
};

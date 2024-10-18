import {
  ApplicationConfig,
  importProvidersFrom,
  LOCALE_ID,
  provideExperimentalZonelessChangeDetection,
} from '@angular/core';
import { provideRouter, TitleStrategy, withComponentInputBinding } from '@angular/router';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { routes } from './app.routes';

import {
  provideHttpClient,
  withFetch,
  withInterceptors,
  withInterceptorsFromDi,
  withXsrfConfiguration,
} from '@angular/common/http';

import { authTokenInterceptor, defaultInterceptor } from './core/http.Interceptor';
import { PageTitleStrategy } from './core/page-title-strategy';
import { icons } from './core/icons-provider';
import { provideNzIcons } from 'ng-zorro-antd/icon';
import { provideNzI18n, zh_CN } from 'ng-zorro-antd/i18n';
import { NzConfig, provideNzConfig } from 'ng-zorro-antd/core/config';

const ngZorroConfig: NzConfig = {
  message: { nzDuration: 2000, nzMaxStack: 3 },
  notification: {
    nzTop: '4.7rem',
    nzDuration: 1000 * 10,
    nzPlacement: 'topRight',
  },
};

export const appConfig: ApplicationConfig = {
  providers: [
    provideHttpClient(
      withFetch(),
      withInterceptorsFromDi(),
      withInterceptors([defaultInterceptor, authTokenInterceptor]),
      withXsrfConfiguration({
        cookieName: 'XSRF-TOKEN',
        headerName: 'X-XSRF-TOKEN',
      })
    ),
    { provide: LOCALE_ID, useValue: 'zh-CN' },
    { provide: TitleStrategy, useClass: PageTitleStrategy },
    provideNzIcons(icons),
    provideNzI18n(zh_CN),
    provideNzConfig(ngZorroConfig),
    provideExperimentalZonelessChangeDetection(),
    provideRouter(routes, withComponentInputBinding()),
    importProvidersFrom(BrowserAnimationsModule),
    provideAnimationsAsync(),
  ],
};

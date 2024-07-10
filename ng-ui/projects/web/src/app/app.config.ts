import {
  ApplicationConfig,
  importProvidersFrom,
  inject,
  LOCALE_ID,
  provideExperimentalZonelessChangeDetection,
} from '@angular/core';
import { provideRouter, TitleStrategy, withComponentInputBinding } from '@angular/router';

import { routes } from './app.routes';
import { NzConfig, provideNzConfig } from 'ng-zorro-antd/core/config';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { PageTitleStrategy } from '../shared/title-strategy.service';
import {
  provideHttpClient,
  withFetch,
  withInterceptors,
  withInterceptorsFromDi,
  withXsrfConfiguration,
} from '@angular/common/http';
import { authTokenInterceptor, defaultInterceptor } from '../core/http.Interceptor';
import { en_US, NZ_I18N, zh_CN } from 'ng-zorro-antd/i18n';

import dayjs from 'dayjs';
import isLeapYear from 'dayjs/plugin/isLeapYear';
import 'dayjs/locale/zh-cn';

dayjs.extend(isLeapYear);
dayjs.locale('zh-cn');

export const ngZorroConfig: NzConfig = {
  message: {
    nzTop: 50,
    nzDuration: 5000,
    nzAnimate: true,
    nzPauseOnHover: true,
  },
  notification: { nzTop: 240 },
};

export const appConfig: ApplicationConfig = {
  providers: [
    importProvidersFrom(BrowserAnimationsModule),
    provideAnimationsAsync(),
    provideNzConfig(ngZorroConfig),
    provideHttpClient(
      withFetch(),
      withInterceptorsFromDi(),
      withInterceptors([defaultInterceptor, authTokenInterceptor]),
      withXsrfConfiguration({
        cookieName: 'XSRF-TOKEN',
        headerName: 'X-XSRF-TOKEN',
      })
    ),
    { provide: TitleStrategy, useClass: PageTitleStrategy },
    {
      provide: NZ_I18N,
      useFactory: () => {
        const localId = inject(LOCALE_ID);
        switch (localId) {
          case 'en':
            return en_US;
          case 'zh':
            return zh_CN;
          default:
            return zh_CN;
        }
      },
    },
    provideExperimentalZonelessChangeDetection(),
    provideRouter(routes, withComponentInputBinding()),
    provideAnimationsAsync(),
  ],
};

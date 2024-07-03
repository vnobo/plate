import {ApplicationConfig, importProvidersFrom, provideExperimentalZonelessChangeDetection,} from '@angular/core';
import {provideRouter, TitleStrategy} from '@angular/router';

import {NzConfig, provideNzConfig} from 'ng-zorro-antd/core/config';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {provideAnimationsAsync} from '@angular/platform-browser/animations/async';
import {
  provideHttpClient,
  withFetch,
  withInterceptors,
  withInterceptorsFromDi,
  withXsrfConfiguration,
} from '@angular/common/http';
import {BrowserStorageServerService, BrowserStorageService,} from 'plate-commons';
import {authTokenInterceptor, defaultInterceptor,} from '../core/http.Interceptor';
import {PageTitleStrategy} from '../core/title-strategy.service';
import {routes} from './app.routes';

export const ngZorroConfig: NzConfig = {
  // 注意组件名称没有 nz 前缀
  message: {
    nzTop: 50,
    nzDuration: 5000,
    nzAnimate: true,
    nzPauseOnHover: true,
  },
  notification: {nzTop: 240},
};

export const appConfig: ApplicationConfig = {
  providers: [
    importProvidersFrom(BrowserAnimationsModule),
    provideAnimationsAsync(),
    provideNzConfig(ngZorroConfig),
    provideRouter(routes),
    provideExperimentalZonelessChangeDetection(),
    {provide: TitleStrategy, useClass: PageTitleStrategy},
    provideHttpClient(
      withFetch(),
      withInterceptorsFromDi(),
      withInterceptors([defaultInterceptor, authTokenInterceptor]),
      withXsrfConfiguration({
        cookieName: 'XSRF-TOKEN',
        headerName: 'X-XSRF-TOKEN',
      })
    ),
    {provide: BrowserStorageService, useClass: BrowserStorageServerService},
  ],
};

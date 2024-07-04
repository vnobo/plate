import {ApplicationConfig, importProvidersFrom, provideZoneChangeDetection} from '@angular/core';
import {provideRouter, TitleStrategy} from '@angular/router';

import {routes} from './app.routes';
import {NzConfig, provideNzConfig} from 'ng-zorro-antd/core/config';
import {provideAnimationsAsync} from '@angular/platform-browser/animations/async';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {PageTitleStrategy} from '../core/title-strategy.service';
import {
  provideHttpClient,
  withFetch,
  withInterceptors,
  withInterceptorsFromDi,
  withXsrfConfiguration,
} from '@angular/common/http';
import {authTokenInterceptor, defaultInterceptor} from '../core/http.Interceptor';
import {BrowserStorageServerService, BrowserStorageService} from 'plate-commons';
import {provideClientHydration} from "@angular/platform-browser";

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

/**
 * provideExperimentalZonelessChangeDetection(),
 */
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
    {provide: TitleStrategy, useClass: PageTitleStrategy},
    {provide: BrowserStorageService, useClass: BrowserStorageServerService},
    provideZoneChangeDetection({eventCoalescing: true}), provideRouter(routes), provideClientHydration()
  ],
};

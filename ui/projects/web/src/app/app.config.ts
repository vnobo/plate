import {ApplicationConfig, importProvidersFrom, provideExperimentalZonelessChangeDetection,} from '@angular/core';
import {provideRouter, TitleStrategy} from '@angular/router';

import {routes} from './app.routes';
import {PageTitleStrategy} from "../core/title-strategy.service";
import {provideNzConfig} from "ng-zorro-antd/core/config";
import {ngZorroConfig} from "../shared/shared-zorro.module";
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {provideAnimationsAsync} from "@angular/platform-browser/animations/async";
import {
  provideHttpClient,
  withFetch,
  withInterceptors,
  withInterceptorsFromDi,
  withXsrfConfiguration
} from "@angular/common/http";
import {authTokenInterceptor, defaultInterceptor} from "../core/http.Interceptor";

export const appConfig: ApplicationConfig = {
  providers: [
    importProvidersFrom(BrowserAnimationsModule),
    provideAnimationsAsync(),
    provideNzConfig(ngZorroConfig),
    provideRouter(routes),
    provideExperimentalZonelessChangeDetection(),
    {provide: TitleStrategy, useClass: PageTitleStrategy},
    provideHttpClient(
      withFetch(), withInterceptorsFromDi(),
      withInterceptors([defaultInterceptor, authTokenInterceptor]),
      withXsrfConfiguration({cookieName: 'XSRF-TOKEN', headerName: 'X-XSRF-TOKEN'})
    )
  ]
};

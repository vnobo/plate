import {
  ApplicationConfig,
  isDevMode,
  LOCALE_ID,
  provideBrowserGlobalErrorListeners,
  provideZonelessChangeDetection,
} from '@angular/core';
import {
  provideClientHydration,
  withEventReplay,
  withI18nSupport,
  withIncrementalHydration,
} from '@angular/platform-browser';
import { provideRouter, withComponentInputBinding } from '@angular/router';

import { provideServiceWorker } from '@angular/service-worker';
import { routes } from './app.routes';

import { indexInterceptor } from '@app/core';

import {
  provideHttpClient,
  withInterceptors,
  withInterceptorsFromDi,
  withXsrfConfiguration,
} from '@angular/common/http';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideZonelessChangeDetection(),
    provideRouter(routes, withComponentInputBinding()),
    provideClientHydration(withEventReplay(), withI18nSupport()),
    provideHttpClient(
      withInterceptorsFromDi(),
      withInterceptors(indexInterceptor),
      withXsrfConfiguration({
        cookieName: 'XSRF-TOKEN',
        headerName: 'X-XSRF-TOKEN',
      }),
    ),
    { provide: LOCALE_ID, useValue: 'zh-Hans' },
    provideServiceWorker('ngsw-worker.js', {
      enabled: !isDevMode(),
      registrationStrategy: 'registerWhenStable:30000',
    }),
  ],
};

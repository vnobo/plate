import {
  ApplicationConfig,
  isDevMode,
  LOCALE_ID,
  provideBrowserGlobalErrorListeners,
  provideZonelessChangeDetection,
} from '@angular/core';
import { provideRouter, withComponentInputBinding } from '@angular/router';

import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { provideClientHydration, withIncrementalHydration } from '@angular/platform-browser';
import { provideServiceWorker } from '@angular/service-worker';
import {
  provideHttpClient,
  withFetch,
  withInterceptors,
  withInterceptorsFromDi,
  withXsrfConfiguration,
} from '@angular/common/http';

import { routes } from '@app/app.routes';
import { indexInterceptor } from '@app/core';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideZonelessChangeDetection(),
    provideRouter(routes, withComponentInputBinding()),
    provideClientHydration(withIncrementalHydration()),
    provideServiceWorker('ngsw-worker.js', {
      enabled: !isDevMode(),
      registrationStrategy: 'registerWhenStable:30000',
    }),
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
  ],
};

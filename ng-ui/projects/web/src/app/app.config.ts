import {ApplicationConfig, provideExperimentalZonelessChangeDetection,} from '@angular/core';
import {provideRouter} from '@angular/router';

import {routes} from './app.routes';

/**
 * provideZoneChangeDetection({eventCoalescing: true})
 */
export const appConfig: ApplicationConfig = {
  providers: [
    provideExperimentalZonelessChangeDetection(),
    provideRouter(routes)
  ],
};

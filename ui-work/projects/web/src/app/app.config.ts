import {
  APP_ID,
  ApplicationConfig,
  importProvidersFrom,
  provideExperimentalZonelessChangeDetection,
} from '@angular/core';
import {provideRouter, TitleStrategy} from '@angular/router';

import {routes} from './app.routes';
import {PageTitleStrategy} from "../core/title-strategy.service";
import {provideNzConfig} from "ng-zorro-antd/core/config";
import {ngZorroConfig} from "../shared/shared-zorro.module";
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {provideAnimationsAsync} from "@angular/platform-browser/animations/async";
import {provideHttpClient, withFetch} from "@angular/common/http";

export const appConfig: ApplicationConfig = {
  providers: [
    {provide: APP_ID, useValue: 'PlateApp'},
    {provide: TitleStrategy, useClass: PageTitleStrategy},
    importProvidersFrom(BrowserAnimationsModule),
    provideAnimationsAsync(),
    provideNzConfig(ngZorroConfig),
    provideRouter(routes),
    provideHttpClient(withFetch()),
    provideExperimentalZonelessChangeDetection()
  ]
};

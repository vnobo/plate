import {APP_ID, ApplicationConfig, importProvidersFrom} from '@angular/core';
import {provideRouter, TitleStrategy} from '@angular/router';

import {routes} from './app.routes';
import {provideNzConfig} from 'ng-zorro-antd/core/config';
import {PageTitleStrategy} from './core/title-strategy.service';
import {CoreModule} from './core/core.module';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {ngZorroConfig} from './shared/shared-zorro.module';

export const appConfig: ApplicationConfig = {
  providers: [
    {provide: APP_ID, useValue: 'PlateApp'},
    {provide: TitleStrategy, useClass: PageTitleStrategy},
    provideRouter(routes),
    importProvidersFrom(CoreModule),
    importProvidersFrom(BrowserAnimationsModule),
    provideNzConfig(ngZorroConfig)
  ],
};

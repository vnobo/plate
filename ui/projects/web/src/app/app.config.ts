import {APP_ID, ApplicationConfig, importProvidersFrom} from '@angular/core';
import {provideRouter, TitleStrategy} from '@angular/router';

import {routes} from './app.routes';
import {NzConfig, provideNzConfig} from 'ng-zorro-antd/core/config';
import {PageTitleStrategy} from './core/title-strategy.service';
import {CoreModule} from './core/core.module';

const ngZorroConfig: NzConfig = {
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
    {provide: APP_ID, useValue: 'PlateApp'},
    {provide: TitleStrategy, useClass: PageTitleStrategy},
    provideRouter(routes),
    provideNzConfig(ngZorroConfig),
    importProvidersFrom(CoreModule),
  ],
};

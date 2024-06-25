import {ApplicationConfig, mergeApplicationConfig} from '@angular/core';
import {provideServerRendering} from '@angular/platform-server';
import {appConfig} from './app.config';
import {BrowserStorageServerService, BrowserStorageService,} from 'commons';

const serverConfig: ApplicationConfig = {
  providers: [
    provideServerRendering(),
    {
      provide: BrowserStorageService,
      useClass: BrowserStorageServerService,
    },
  ],
};

export const config = mergeApplicationConfig(appConfig, serverConfig);

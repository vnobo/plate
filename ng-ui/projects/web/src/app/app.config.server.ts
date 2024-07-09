import { ApplicationConfig, mergeApplicationConfig } from '@angular/core';
import { provideServerRendering } from '@angular/platform-server';
import { appConfig } from './app.config';
import {
  BrowserStorageServerService,
  BrowserStorageService,
  SessionStorageServerService,
  SessionStorageService,
} from 'plate-commons';

const serverConfig: ApplicationConfig = {
  providers: [
    provideServerRendering(),
    { provide: BrowserStorageService, useClass: BrowserStorageServerService },
    { provide: SessionStorageService, useClass: SessionStorageServerService },
  ],
};

export const config = mergeApplicationConfig(appConfig, serverConfig);

import {APP_ID, isDevMode, LOCALE_ID, NgModule} from '@angular/core';
import {BrowserModule} from '@angular/platform-browser';

import {AppRoutingModule} from './app-routing.module';
import {AppComponent} from './app.component';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {ServiceWorkerModule} from '@angular/service-worker';
import {RouterModule} from '@angular/router';
import {httpInterceptorProviders} from "./security/http-interceptors";
import {HttpClientXsrfModule} from "@angular/common/http";
import {SharedModule} from "./shared/shared.module";

@NgModule({
  declarations: [
    AppComponent
  ],
  imports: [
    ServiceWorkerModule.register('ngsw-worker.js', {
      enabled: !isDevMode(),
      // Register the ServiceWorker as soon as the application is stable
      // or after 30 seconds (whichever comes first).
      registrationStrategy: 'registerWhenStable:30000'
    }),
    BrowserAnimationsModule,
    HttpClientXsrfModule.withOptions({
      cookieName: 'XSRF-TOKEN',
      headerName: 'X-XSRF-TOKEN'
    }),
    RouterModule,
    AppRoutingModule,
    SharedModule
  ],
  providers: [
    httpInterceptorProviders,
    {provide: LOCALE_ID, useValue: 'zh-Hans'},
    {provide: APP_ID, useValue: 'serverApp'}
  ],
  bootstrap: [AppComponent]
})
export class AppModule {
}

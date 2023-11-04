import {APP_ID, isDevMode, NgModule} from '@angular/core';

import {AppComponent} from './app.component';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {ServiceWorkerModule} from '@angular/service-worker';
import {TitleStrategy} from '@angular/router';
import {HttpClientXsrfModule} from "@angular/common/http";
import {SharedModule} from "./shared/shared.module";
import {PagesModule} from "./pages/pages.module";
import {PageTitleStrategy} from "./core/title-strategy.service";
import {CoreModule} from "./core/core.module";

@NgModule({
  declarations: [AppComponent],
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
    CoreModule,
    SharedModule,
    PagesModule
  ],
  providers: [
    {provide: APP_ID, useValue: 'serverApp'},
    {provide: TitleStrategy, useClass: PageTitleStrategy}
  ],
  bootstrap: [AppComponent]
})
export class AppModule {
}

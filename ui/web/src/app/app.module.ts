import {APP_ID, isDevMode, LOCALE_ID, NgModule} from '@angular/core';

import {AppComponent} from './app.component';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {ServiceWorkerModule} from '@angular/service-worker';
import {TitleStrategy} from '@angular/router';
import {SharedModule} from "./shared/shared.module";
import {PageTitleStrategy} from "./core/title-strategy.service";
import {CoreModule} from "./core/core.module";
import {AppRoutingModule} from "./app-routing.module";
import {GlobalConfigModule} from "./global-config.module";

@NgModule({
  declarations: [AppComponent],
  imports: [
    ServiceWorkerModule.register('ngsw-worker.js', {
      enabled: !isDevMode(),
      // Register the ServiceWorker as soon as the application is stable
      // or after 30 seconds (whichever comes first).
      registrationStrategy: 'registerWhenStable:30000'
    }),
    GlobalConfigModule.forRoot(),
    BrowserAnimationsModule,
    AppRoutingModule,
    CoreModule,
    SharedModule
  ],
  providers: [
    {provide: LOCALE_ID, useValue: 'zh'},
    {provide: APP_ID, useValue: 'PlateApp'},
    {provide: TitleStrategy, useClass: PageTitleStrategy}
  ],
  bootstrap: [AppComponent]
})
export class AppModule {
}

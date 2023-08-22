import {registerLocaleData} from '@angular/common';
import zh from '@angular/common/locales/zh';
import {APP_ID, isDevMode, LOCALE_ID, NgModule} from '@angular/core';

import {AppRoutingModule} from './app-routing.module';
import {AppComponent} from './app.component';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {ServiceWorkerModule} from '@angular/service-worker';
import {RouterModule} from '@angular/router';
import {httpInterceptorProviders} from "./security/http-interceptors";
import {HttpClientXsrfModule} from "@angular/common/http";
import {SharedModule} from "./shared/shared.module";
import {NzSliderModule} from "ng-zorro-antd/slider";
import {NzLayoutModule} from "ng-zorro-antd/layout";
import {NzMenuModule} from "ng-zorro-antd/menu";
import {NzIconModule} from "ng-zorro-antd/icon";

registerLocaleData(zh);

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
        SharedModule,
        NzSliderModule,
        NzLayoutModule,
        NzMenuModule,
        NzIconModule
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

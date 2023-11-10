import {NgModule, Optional, SkipSelf} from '@angular/core';
import {HttpClientModule, HttpClientXsrfModule} from "@angular/common/http";
import {httpInterceptorProviders} from "./http-interceptors";

@NgModule({
  imports: [HttpClientModule,
    HttpClientXsrfModule.withOptions({
      cookieName: 'XSRF-TOKEN',
      headerName: 'X-XSRF-TOKEN'
    })],
  exports: [
    HttpClientModule,
    HttpClientXsrfModule
  ],
  providers: [httpInterceptorProviders]
})
export class CoreModule {
  constructor(@Optional() @SkipSelf() parentModule?: CoreModule) {
    if (parentModule) {
      throw new Error(
        'CoreModule is already loaded. Import it in the AppModule only');
    }
  }
}

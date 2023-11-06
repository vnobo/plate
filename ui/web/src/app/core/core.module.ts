import {NgModule, Optional, SkipSelf} from '@angular/core';
import {httpInterceptorProviders} from "./http-interceptors";
import {HttpClientModule} from "@angular/common/http";


@NgModule({
  imports: [
    HttpClientModule
  ],
  providers: [
    httpInterceptorProviders
  ]
})
export class CoreModule {
  constructor(@Optional() @SkipSelf() parentModule?: CoreModule) {
    if (parentModule) {
      throw new Error(
        'CoreModule is already loaded. Import it in the AppModule only');
    }
  }
}

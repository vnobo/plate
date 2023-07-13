import {NgModule, Optional, SkipSelf} from '@angular/core';

import {SecurityRoutingModule} from './security-routing.module';
import {LoginComponent} from './login/login.component';
import {SharedModule} from "../shared/shared.module";
import {MAT_SNACK_BAR_DEFAULT_OPTIONS} from "@angular/material/snack-bar";


@NgModule({
  declarations: [
    LoginComponent
  ],
  imports: [
    SharedModule,
    SecurityRoutingModule
  ],
  providers: [
    {provide: MAT_SNACK_BAR_DEFAULT_OPTIONS, useValue: {duration: 2500}}
  ]
})
export class SecurityModule {
  constructor(@Optional() @SkipSelf() parentModule?: SecurityModule) {
    if (parentModule) {
      throw new Error(
        'SecurityModule is already loaded. Import it in the AppModule only');
    }
  }
}

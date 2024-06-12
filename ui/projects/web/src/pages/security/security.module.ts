import {NgModule, Optional, SkipSelf} from '@angular/core';

import {SecurityRoutingModule} from './security-routing.module';
import {LoginComponent} from './login/login.component';
import {NzDividerModule} from "ng-zorro-antd/divider";
import {SharedModule} from '../../shared/shared.module';
import {LoginV1Component} from "./loginv1/login.component";
import {NzCheckboxModule} from "ng-zorro-antd/checkbox";

@NgModule({
  imports: [
    SharedModule,
    SecurityRoutingModule,
    NzDividerModule,
    NzCheckboxModule
  ],
  declarations: [
    LoginComponent,
    LoginV1Component
  ],
  exports: [
    LoginComponent,
    LoginV1Component
  ],
  providers: []
})
export class SecurityModule {
  constructor(@Optional() @SkipSelf() parentModule?: SecurityModule) {
    if (parentModule) {
      throw new Error(
        'SecurityModule is already loaded. Import it in the AppModule only');
    }
  }
}

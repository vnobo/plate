import {NgModule, Optional, SkipSelf} from '@angular/core';

import {SecurityRoutingModule} from './security-routing.module';
import {LoginComponent} from './login/login.component';
import {NzDividerModule} from "ng-zorro-antd/divider";
import {SharedModule} from '../../shared/shared.module';


@NgModule({
  imports: [
    SharedModule,
    SecurityRoutingModule,
    NzDividerModule
  ],
  declarations: [
    LoginComponent
  ],
  exports: [
    LoginComponent
  ],
  providers: [
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

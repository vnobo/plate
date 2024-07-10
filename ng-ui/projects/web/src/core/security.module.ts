import { NgModule, Optional, SkipSelf } from '@angular/core';
import { SecurityRoutingModule } from './security-routing.module';

@NgModule({
  imports: [SecurityRoutingModule],
  providers: [],
})
export class SecurityModule {
  constructor(@Optional() @SkipSelf() parentModule?: SecurityModule) {
    if (parentModule) {
      throw new Error('SecurityModule is already loaded. Import it in the AppModule only');
    }
  }
}

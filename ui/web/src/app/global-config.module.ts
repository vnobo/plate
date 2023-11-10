import {ModuleWithProviders, NgModule, Optional, SkipSelf} from '@angular/core';

@NgModule({
  imports: [],
  exports: [],
  providers: []
})
export class GlobalConfigModule {
  constructor(@Optional() @SkipSelf() parentModule: GlobalConfigModule) {
    if (parentModule) {
      throw new Error(
        'GlobalConfigModule is already loaded. Import it in the AppModule only');
    }
  }

  static forRoot(): ModuleWithProviders<GlobalConfigModule> {
    return {
      ngModule: GlobalConfigModule
    };
  }
}

import {ModuleWithProviders, NgModule, Optional, SkipSelf} from '@angular/core';
import {CommonModule} from "@angular/common";
import {NzConfig, provideNzConfig} from "ng-zorro-antd/core/config";

const ngZorroConfig: NzConfig = {
  // 注意组件名称没有 nz 前缀
  message: {
    nzTop: 50,
    nzDuration: 5000,
    nzAnimate: true,
    nzPauseOnHover: true
  },
  notification: {nzTop: 240}
};

@NgModule({
  imports: [CommonModule],
  exports: [],
  providers: [provideNzConfig(ngZorroConfig)]
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

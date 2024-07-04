import {NgModule, Optional, SkipSelf} from '@angular/core';

import {PagesRoutingModule} from './pages.routes';

import {IndexComponent} from './home/index/index.component';
import {HomeComponent} from './home/home.component';
import {SharedModule} from '../shared/shared.module';
import {NzLayoutModule} from 'ng-zorro-antd/layout';
import {NzMenuModule} from 'ng-zorro-antd/menu';
import {NzSliderModule} from 'ng-zorro-antd/slider';

@NgModule({
  imports: [
    NzLayoutModule,
    NzSliderModule,
    NzMenuModule,
    SharedModule,
    PagesRoutingModule,
  ],
  declarations: [IndexComponent, HomeComponent],
  exports: [],
})
export class PagesModule {
  constructor(@Optional() @SkipSelf() parentModule?: PagesModule) {
    if (parentModule) {
      throw new Error(
        'PagesModule is already loaded. Import it in the AppModule only'
      );
    }
  }
}

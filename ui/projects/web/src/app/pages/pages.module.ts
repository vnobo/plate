import {NgModule, Optional, SkipSelf} from '@angular/core';

import {PagesRoutingModule} from './pages-routing.module';

import {IndexComponent} from './home/index/index.component';
import {PageNotFoundComponent} from "./page-not-found/page-not-found.component";
import {SharedModule} from "../shared/shared.module";
import {HomeComponent} from './home/home.component';

@NgModule({
  imports: [
    SharedModule,
    PagesRoutingModule
  ],
  declarations: [IndexComponent, PageNotFoundComponent, HomeComponent],
  exports: [PageNotFoundComponent]
})
export class PagesModule {
  constructor(@Optional() @SkipSelf() parentModule?: PagesModule) {
    if (parentModule) {
      throw new Error(
        'PagesModule is already loaded. Import it in the AppModule only');
    }
  }
}
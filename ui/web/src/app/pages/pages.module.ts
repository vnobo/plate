import {NgModule} from '@angular/core';

import {PagesRoutingModule} from './pages-routing.module';

import {WelcomeComponent} from './welcome/welcome.component';
import {NzIconModule} from "ng-zorro-antd/icon";
import {NzLayoutModule} from "ng-zorro-antd/layout";
import {NzMenuModule} from "ng-zorro-antd/menu";
import {NzSliderModule} from "ng-zorro-antd/slider";
import {NzResultModule} from "ng-zorro-antd/result";

@NgModule({
  imports: [
    PagesRoutingModule,
    NzIconModule,
    NzLayoutModule,
    NzMenuModule,
    NzSliderModule,
    NzLayoutModule,
    NzMenuModule,
    NzIconModule,
    NzResultModule
  ],
  declarations: [WelcomeComponent],
  exports: [WelcomeComponent]
})
export class PagesModule {
}

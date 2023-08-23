import {NgModule} from '@angular/core';

import {WelcomeRoutingModule} from './welcome-routing.module';

import {WelcomeComponent} from './welcome.component';
import {NzIconModule} from "ng-zorro-antd/icon";
import {NzLayoutModule} from "ng-zorro-antd/layout";
import {NzMenuModule} from "ng-zorro-antd/menu";
import {NzSliderModule} from "ng-zorro-antd/slider";

@NgModule({
  imports: [
    WelcomeRoutingModule,
    NzIconModule,
    NzLayoutModule,
    NzMenuModule,
    NzSliderModule,
    NzLayoutModule,
    NzMenuModule,
    NzIconModule
  ],
  declarations: [WelcomeComponent],
  exports: [WelcomeComponent]
})
export class WelcomeModule {
}

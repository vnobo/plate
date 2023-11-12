import {NgModule} from '@angular/core';
import {NzSpinModule} from "ng-zorro-antd/spin";
import {NzBackTopModule} from "ng-zorro-antd/back-top";
import {NzResultModule} from "ng-zorro-antd/result";
import {NzLayoutModule} from "ng-zorro-antd/layout";
import {NzMenuModule} from "ng-zorro-antd/menu";
import {NzSliderModule} from "ng-zorro-antd/slider";
import {NzBreadCrumbModule} from "ng-zorro-antd/breadcrumb";
import {NzButtonModule} from "ng-zorro-antd/button";
import {NzTableModule} from "ng-zorro-antd/table";
import {NzMessageModule} from "ng-zorro-antd/message";
import {NzModalModule} from "ng-zorro-antd/modal";
import {NzFormModule} from "ng-zorro-antd/form";
import {NzInputModule} from "ng-zorro-antd/input";


@NgModule({
  exports: [
    NzMessageModule,
    NzSpinModule,
    NzBackTopModule,
    NzResultModule,
    NzLayoutModule,
    NzMenuModule,
    NzSliderModule,
    NzLayoutModule,
    NzMenuModule,
    NzResultModule,
    NzBreadCrumbModule,
    NzButtonModule,
    NzTableModule,
    NzModalModule,
    NzFormModule,
    NzInputModule
  ]
})
export class SharedZorroModule {
}

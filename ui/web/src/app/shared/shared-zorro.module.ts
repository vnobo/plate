import {NgModule} from '@angular/core';
import {NzSpinModule} from "ng-zorro-antd/spin";
import {NzBackTopModule} from "ng-zorro-antd/back-top";
import {NzResultModule} from "ng-zorro-antd/result";
import {MatSnackBarModule} from "@angular/material/snack-bar";
import {MatProgressBarModule} from "@angular/material/progress-bar";
import {NzIconModule} from "ng-zorro-antd/icon";
import {NzLayoutModule} from "ng-zorro-antd/layout";
import {NzMenuModule} from "ng-zorro-antd/menu";
import {NzSliderModule} from "ng-zorro-antd/slider";


@NgModule({
  exports: [
    NzSpinModule,
    NzBackTopModule,
    NzResultModule,
    MatSnackBarModule,
    MatProgressBarModule,
    NzIconModule,
    NzLayoutModule,
    NzMenuModule,
    NzSliderModule,
    NzLayoutModule,
    NzMenuModule,
    NzIconModule,
    NzResultModule
  ]
})
export class SharedZorroModule {
}

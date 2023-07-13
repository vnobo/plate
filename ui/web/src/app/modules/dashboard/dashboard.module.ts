import {NgModule} from '@angular/core';

import {DashboardRoutingModule} from './dashboard-routing.module';
import {IndexComponent} from "./index/index.component";
import {SharedModule} from "../../shared/shared.module";


@NgModule({
  declarations: [
    IndexComponent
  ],
  imports: [
    SharedModule,
    DashboardRoutingModule
  ]
})
export class DashboardModule {
}

import {NgModule} from '@angular/core';
import {MenusComponent} from './menus/menus.component';
import {SystemRoutingModule} from "./system-routing.module";
import {SharedModule} from "../../shared/shared.module";


@NgModule({
  declarations: [
    MenusComponent
  ],
  imports: [
    SharedModule,
    SystemRoutingModule
  ]
})
export class SystemModule {
}

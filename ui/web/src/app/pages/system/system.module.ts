import {NgModule} from '@angular/core';
import {MenusComponent} from './menus/menus.component';
import {SystemRoutingModule} from "./system-routing.module";
import {SharedModule} from "../../shared/shared.module";
import {MenuFormComponent} from './menus/menu-form.component';


@NgModule({
  declarations: [
    MenusComponent,
    MenuFormComponent
  ],
  imports: [
    SharedModule,
    SystemRoutingModule
  ]
})
export class SystemModule {
}

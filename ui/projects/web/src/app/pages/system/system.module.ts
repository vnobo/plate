import {NgModule} from '@angular/core';
import {MenusComponent} from './menus/menus.component';
import {SystemRoutingModule} from "./system.routes";
import {MenuFormComponent} from './menus/menu-form.component';
import {SharedModule} from '../../shared/shared.module';


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

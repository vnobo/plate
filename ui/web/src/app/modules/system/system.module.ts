import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';

import {SystemRoutingModule} from './system-routing.module';
import {MenusComponent} from './menus/menus.component';
import {UsersComponent} from './users/users.component';
import {SharedModule} from "../../shared/shared.module";

@NgModule({
  declarations: [
    MenusComponent,
    UsersComponent
  ],
  imports: [
    CommonModule,
    SharedModule,
    SystemRoutingModule
  ]
})
export class SystemModule {
}

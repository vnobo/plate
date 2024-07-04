import {NgModule} from '@angular/core';
import {MenusComponent} from './menus/menus.component';
import {SystemRoutingModule} from './system.routes';
import {MenuFormComponent} from './menus/menu-form.component';
import {SharedModule} from '../../shared/shared.module';
import {GroupsComponent} from './groups/groups.component';
import {UsersComponent} from './users/users.component';

@NgModule({
  declarations: [
    MenusComponent,
    MenuFormComponent,
    GroupsComponent,
    UsersComponent,
  ],
  imports: [SharedModule, SystemRoutingModule],
})
export class SystemModule {
}

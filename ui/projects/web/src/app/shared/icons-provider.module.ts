import {NgModule} from '@angular/core';
import {NzIconModule} from 'ng-zorro-antd/icon';
import {UserOutline} from '@ant-design/icons-angular/icons';

const icons = [UserOutline];

@NgModule({
  imports: [NzIconModule.forRoot(icons)],
  exports: [NzIconModule]
})
export class IconsProviderModule {
}

import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {PageComponent} from "./page/page.component";

@NgModule({
  declarations: [],
  imports: [
    CommonModule,
    PageComponent
  ],
  exports: [PageComponent]
})
export class LayoutModule {
}

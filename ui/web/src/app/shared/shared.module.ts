import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {SharedZorroModule} from "./shared-zorro.module";
import {IconsProviderModule} from "./icons-provider.module";
import {RouterModule} from "@angular/router";

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    RouterModule,
    ReactiveFormsModule,
    IconsProviderModule,
    SharedZorroModule
  ],
  declarations: [],
  exports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    RouterModule,
    IconsProviderModule,
    SharedZorroModule
  ]
})
export class SharedModule {
}

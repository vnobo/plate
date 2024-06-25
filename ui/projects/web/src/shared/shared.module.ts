import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {SharedZorroModule} from './shared-zorro.module';
import {RouterModule} from '@angular/router';
import {IconsProviderModule} from './icons-provider.module';

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    RouterModule,
    ReactiveFormsModule,
    IconsProviderModule,
    SharedZorroModule,
  ],
  declarations: [],
  exports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    RouterModule,
    SharedZorroModule,
    IconsProviderModule,
  ],
})
export class SharedModule {
}

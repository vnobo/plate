import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { SHARED_ZORRO_MODULES } from './shared-zorro.module';
import { NgModule, Type } from '@angular/core';
import { TruncateMiddlePipe } from './truncate-middle.pipe';
import { MATERIAL_MODULES } from './material.module';

// #region your componets & directives
const COMPONENTS: Array<Type<any>> = [];
const DIRECTIVES: Array<Type<any>> = [TruncateMiddlePipe];

@NgModule({
  imports: [CommonModule, FormsModule, RouterModule, ReactiveFormsModule, ...SHARED_ZORRO_MODULES],
  declarations: [
    // your components
    ...COMPONENTS,
    ...DIRECTIVES,
  ],
  exports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    RouterModule,
    ...MATERIAL_MODULES,
    ...SHARED_ZORRO_MODULES,
    // your components
    ...COMPONENTS,
    ...DIRECTIVES,
  ],
})
export class SharedModule {
}

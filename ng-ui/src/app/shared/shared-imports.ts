import {CommonModule, DatePipe} from '@angular/common';
import {NgModule, Type} from '@angular/core';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {PageHeaderComponent} from '@app/layout';
import {MATERIAL_MODULES} from './material.module';
import {SHARED_ZORRO_MODULES} from './shared-zorro.module';
import {TruncateMiddlePipe} from './truncate-middle.pipe';

// #region your standalone componets & directives
export const COMPONENTS: Array<Type<any>> = [PageHeaderComponent];
export const DIRECTIVES: Array<Type<any>> = [];

@NgModule({
  imports: [TruncateMiddlePipe, DatePipe],
  exports: [TruncateMiddlePipe, DatePipe],
})
export class ImportsModule {
}

export const SHARED_IMPORTS: Array<Type<any>> = [
  CommonModule,
  FormsModule,
  ReactiveFormsModule,
  ImportsModule,
  ...MATERIAL_MODULES,
  ...SHARED_ZORRO_MODULES,
  ...COMPONENTS,
  ...DIRECTIVES,
];

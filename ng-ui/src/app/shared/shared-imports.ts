import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { PageHeaderComponent } from '@app/layout';
import { MATERIAL_MODULES } from './material.module';
import { SHARED_ZORRO_MODULES } from './shared-zorro.module';

// #region your standalone componets & directives
export const COMPONENTS = [PageHeaderComponent];
export const DIRECTIVES = [];

export const SHARED_IMPORTS = [
  CommonModule,
  FormsModule,
  ReactiveFormsModule,
  ...MATERIAL_MODULES,
  ...SHARED_ZORRO_MODULES,
  ...COMPONENTS,
  ...DIRECTIVES,
];

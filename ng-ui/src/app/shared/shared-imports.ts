import { NgClass, NgStyle, NgTemplateOutlet } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { PageHeaderComponent, PageHeaderComponentDirective } from '@app/layout';
import { SHARED_ZORRO_MODULES } from './shared-zorro.module';

export const LAYOUT_COMPONENTS = [PageHeaderComponent, PageHeaderComponentDirective];

export const SHARED_IMPORTS = [
  FormsModule,
  ReactiveFormsModule,
  RouterLink,
  NgTemplateOutlet,
  NgClass,
  NgStyle,
  ...SHARED_ZORRO_MODULES,
  ...LAYOUT_COMPONENTS,
];

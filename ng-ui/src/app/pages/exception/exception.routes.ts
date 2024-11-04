import { Routes } from '@angular/router';

import { ExceptionComponent } from './exception.component';
import { ExceptionTriggerComponent } from './trigger.component';
import { PageNotFoundComponent } from './not-found.component';

export const EXCEPTION_ROUTES: Routes = [
  { path: '403', component: ExceptionComponent, data: { type: 403 } },
  { path: '404', component: PageNotFoundComponent, data: { type: 404 } },
  { path: '500', component: ExceptionComponent, data: { type: 500 } },
  { path: 'trigger', component: ExceptionTriggerComponent },
];

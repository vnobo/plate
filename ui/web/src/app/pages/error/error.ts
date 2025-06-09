import { Routes } from '@angular/router';

import { Ex500 } from './500';
import { Ex512 } from './512';
import { Ex404 } from './404';

export const EXCEPTION_ROUTES: Routes = [
  { path: '403', component: Ex500, data: { type: 403 } },
  { path: '404', component: Ex404, data: { type: 404 } },
  { path: '500', component: Ex500, data: { type: 500 } },
  { path: 'trigger', component: Ex512 },
];

import { Routes } from '@angular/router';

import { Error500 } from './500';
import { Error512 } from './512';
import { Error404 } from './404';

export const EXCEPTION_ROUTES: Routes = [
  { path: '403', component: Error500, data: { type: 403 } },
  { path: '404', component: Error404, data: { type: 404 } },
  { path: '500', component: Error500, data: { type: 500 } },
  { path: 'trigger', component: Error512 },
  {
    path: '',
    redirectTo: '404',
    pathMatch: 'full',
  },
];

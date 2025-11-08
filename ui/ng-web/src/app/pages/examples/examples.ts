import { Routes } from '@angular/router';
import { TransferDemoComponent } from './transfer-demo';

export const EXAMPLES_ROUTES: Routes = [
  {
    path: 'transfer',
    component: TransferDemoComponent,
    data: { title: 'Transfer Component' },
  },
  {
    path: '',
    redirectTo: 'transfer',
    pathMatch: 'full',
  },
];

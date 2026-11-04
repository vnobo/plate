import { Routes } from '@angular/router';

export const EXAMPLES_ROUTES: Routes = [
  {
    path: 'transfer',
    loadComponent: () => import('./transfer-demo').then((m) => m.TransferDemoComponent),
    data: { title: 'Transfer Component' },
  },
  {
    path: 'data-table',
    loadComponent: () => import('./data-table-demo').then((m) => m.DataTableDemoComponent),
    data: { title: 'Data Table Component' },
  },
  {
    path: '',
    redirectTo: 'transfer',
    pathMatch: 'full',
  },
];

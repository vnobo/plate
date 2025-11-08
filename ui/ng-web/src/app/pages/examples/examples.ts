import { Routes } from '@angular/router';
import { TransferDemoComponent } from './transfer-demo';
import { DataTableDemoComponent } from './data-table-demo';

export const EXAMPLES_ROUTES: Routes = [
  {
    path: 'transfer',
    component: TransferDemoComponent,
    data: { title: 'Transfer Component' },
  },
  {
    path: 'data-table',
    component: DataTableDemoComponent,
    data: { title: 'Data Table Component' },
  },
  {
    path: '',
    redirectTo: 'transfer',
    pathMatch: 'full',
  },
];

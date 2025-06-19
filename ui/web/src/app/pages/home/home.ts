import {Routes} from '@angular/router';
import {BaseLayout} from '@app/layout';
import {Users} from './users/users';

export const HOME_ROUTES: Routes = [
  {
    path: '',
    component: BaseLayout,
    children: [
      {
        path: 'users',
        component: Users,
      },
      {
        path: '',
        pathMatch: 'full',
        redirectTo: 'users',
      },
    ],
  },
];

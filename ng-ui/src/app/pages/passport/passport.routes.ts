import { Routes } from '@angular/router';
import { UsersComponent } from '../home/users/users.component';

export const PASSPORT_ROUTES: Routes = [
  // passport
  {
    path: 'passport',
    children: [
      {
        path: 'login',
        component: UsersComponent,
        data: { title: '登录', titleI18n: 'app.login.login' },
      },
      /**{
        path: 'register',
        component: UserRegisterComponent,
        data: { title: '注册', titleI18n: 'app.register.register' },
      },
      {
        path: 'register-result',
        component: UserRegisterResultComponent,
        data: { title: '注册结果', titleI18n: 'app.register.register' },
      },
      {
        path: 'lock',
        component: UserLockComponent,
        data: { title: '锁屏', titleI18n: 'app.lock' },
      },*/
    ],
  },
  // 单页不包裹Layout
  //{ path: 'passport/callback/:type', component: CallbackComponent },
];

import { Routes } from '@angular/router';
import { Login } from './login/login';

export const PASSPORT_ROUTES: Routes = [
  {
    path: '',
    children: [
      {
        path: 'login',
        component: Login,
        data: { title: '登录你的系统' },
      },
      {
        path: '',
        pathMatch: 'full',
        redirectTo: 'login',
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

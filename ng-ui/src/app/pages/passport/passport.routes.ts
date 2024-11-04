import { Routes } from '@angular/router';
import { LoginComponent } from './login/login.component';

export const PASSPORT_ROUTES: Routes = [
  // passport
  {
    path: 'login',
    component: LoginComponent,
    data: { title: '登录' },
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
  // 单页不包裹Layout
  //{ path: 'passport/callback/:type', component: CallbackComponent },
];

import { Router, type CanActivateFn } from '@angular/router';
import { AuthService } from '.';
import { inject } from '@angular/core';

// 定义一个函数，用于判断用户是否已登录
export const authGuard: CanActivateFn = (route, state) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  if (auth.isLogged()) {
    return true;
  }
  auth.redirectUrl = state.url;
  return router.parseUrl(auth.loginUrl);
};
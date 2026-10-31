import { inject } from '@angular/core';
import {
  ActivatedRouteSnapshot,
  CanActivateChildFn,
  CanActivateFn,
  Router,
  RouterStateSnapshot,
} from '@angular/router';
import { TokenService } from './services/token.service';

export const canActivateGuard: CanActivateFn = (route, state) => {
  const auth = inject(TokenService);
  const router = inject(Router);
  // 用 authenticationToken() 而不是 isLogged()：令牌存在 sessionStorage，
  // 刷新后内存 signal 是 false，只查 isLogged() 会把已登录用户踢回登录页
  if (auth.authenticationToken()) {
    return true;
  }
  auth.redirectUrl = state.url;
  return router.parseUrl(auth.loginUrl);
};

export const roleChildGuard: CanActivateChildFn = (
  childRoute: ActivatedRouteSnapshot,
  _state: RouterStateSnapshot,
) => {
  const auth = inject(TokenService);
  const role = childRoute.data['role'];
  if (!role) {
    return true;
  }
  return auth.hasRole(role);
};

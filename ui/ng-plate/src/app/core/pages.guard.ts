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
  if (auth.isLogged()) {
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

import {type CanActivateFn, Router} from '@angular/router';
import {AuthService} from '.';
import {inject} from '@angular/core';

export const authGuard: CanActivateFn = (route, state) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  if (auth.isLogged()) {
    return true;
  }
  auth.redirectUrl = state.url;
  return router.parseUrl(auth.loginUrl);
};

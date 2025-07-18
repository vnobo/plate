import { type CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { TokenService } from './services/token.service';

export const authGuard: CanActivateFn = (route, state) => {
  const auth = inject(TokenService);
  const router = inject(Router);
  if (auth.isLogged()) {
    return true;
  }
  auth.redirectUrl = state.url;
  return router.parseUrl(auth.loginUrl);
};

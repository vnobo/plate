import {inject, Injectable} from '@angular/core';
import {Subject} from "rxjs";
import {HttpErrorResponse} from "@angular/common/http";
import {CanActivateChildFn, CanActivateFn, CanMatchFn, Router} from "@angular/router";

/**
 * A function that acts as an authentication guard.
 *
 * @return {CanMatchFn | CanActivateFn | CanActivateChildFn} The authentication result.
 */
export const authGuard: CanMatchFn | CanActivateFn | CanActivateChildFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.isLoggedIn) {
    return true;
  }

  return router.parseUrl(authService.loginUrl);
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  isLoggedIn = false;
  // store the URL so we can redirect after logging in
  loginUrl = '/auth/login';
  private sessionKey = 'x-auth-token';
  private authenticatedSource = new Subject<boolean>();
  authenticated$ = this.authenticatedSource.asObservable();

  authToken(): string {
    const authToken = sessionStorage.getItem(this.sessionKey);
    if (authToken == null) {
      throw new HttpErrorResponse({error: "Authenticate is incorrectness,please login again.", status: 401});
    }
    return authToken;
  }

  login(authentication: string) {
    this.isLoggedIn = true;
    this.authenticatedSource.next(true);
    sessionStorage.setItem(this.sessionKey, authentication);
  }

  logout(): void {
    this.isLoggedIn = false;
    this.authenticatedSource.next(false);
    sessionStorage.removeItem(this.sessionKey);
  }

}

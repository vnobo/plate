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
  const auth = inject(AuthService);
  const router = inject(Router);
  if (auth.isLoggedIn) {
    return true;
  }

  return router.parseUrl(auth.loginUrl);
}

@Injectable({providedIn: 'root'})
export class AuthService {
  isLoggedIn = false;
  // store the URL so we can redirect after logging in
  loginUrl = '/auth/login';
  private sessionKey = 'x-auth-token';
  private authenticatedSource = new Subject<boolean>();
  authenticated$ = this.authenticatedSource.asObservable();

  constructor() {
    this.autoLogin();
  }

  autoLogin() {
    const authToken = this.loadSessionByStorage();
    if (authToken != null) {
      this.isLoggedIn = true;
      this.authenticatedSource.next(true);
    }
  }

  authToken(): string {
    const authToken = this.loadSessionByStorage();
    if (authToken == null) {
      throw new HttpErrorResponse({error: "Authenticate is incorrectness,please login again.", status: 401});
    }
    return authToken;
  }

  loadSessionByStorage(): string | null {
    const authToken = '222222222222222222222222222';//sessionStorage.getItem(this.sessionKey);
    if (authToken != null) {
      return authToken;
    }
    return null;
  }

  login(authentication: string) {
    this.isLoggedIn = true;
    this.authenticatedSource.next(true);
    //sessionStorage.setItem(this.sessionKey, authentication);
  }

  logout(): void {
    this.isLoggedIn = false;
    this.authenticatedSource.next(false);
    //sessionStorage.removeItem(this.sessionKey);
  }

}

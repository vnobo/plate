import {inject, Injectable} from '@angular/core';
import {Subject} from 'rxjs';
import {HttpErrorResponse} from '@angular/common/http';
import {CanActivateChildFn, CanActivateFn, CanMatchFn, Router,} from '@angular/router';

export const authGuard:
  | CanMatchFn
  | CanActivateFn
  | CanActivateChildFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  if (auth.isLoggedIn) {
    return true;
  }
  return router.parseUrl(auth.loginUrl);
};

@Injectable({providedIn: 'root'})
export class AuthService {
  public readonly loginUrl = '/auth/login';
  isLoggedIn = false;
  private authenticatedSource = new Subject<boolean>();
  authenticated$ = this.authenticatedSource.asObservable();
  private token = '';

  authToken(): string {
    if (!this.isLoggedIn) {
      throw new HttpErrorResponse({
        error: 'Authenticate is incorrectness,please login again.',
        status: 401,
      });
    }
    return this.token;
  }

  login(token: string) {
    this.isLoggedIn = true;
    this.authenticatedSource.next(true);
    this.token = token;
  }

  logout(): void {
    this.isLoggedIn = false;
    this.authenticatedSource.next(false);
    this.token = '';
  }
}

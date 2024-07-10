import { HttpErrorResponse } from '@angular/common/http';
import { inject, Injectable, signal } from '@angular/core';
import { toObservable } from '@angular/core/rxjs-interop';
import { CanActivateChildFn, CanActivateFn, CanMatchFn, Router } from '@angular/router';
import { Observable } from 'rxjs';
import dayjs from 'dayjs';
import { SessionStorageService } from '../shared/session-storage.service';

export interface Authentication {
  token: string;
  expires: number;
  lastAccessTime: number;
  details: any;
}

export const authGuard: CanMatchFn | CanActivateFn | CanActivateChildFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  if (auth.isLogged()) {
    return true;
  }
  return router.parseUrl(auth.loginUrl);
};

@Injectable({ providedIn: 'root' })
export class AuthService {
  readonly loginUrl = '/auth/login';
  _storage: SessionStorageService = inject(SessionStorageService);
  private readonly authenticationKey = 'authentication';
  private isLoggedIn = signal(false);
  authenticated$: Observable<boolean> = toObservable(this.isLoggedIn);
  private authentication = signal({} as Authentication);
  authentication$: Observable<Authentication> = toObservable(this.authentication);

  isLogged(): boolean {
    if (this.isLoggedIn()) {
      return true;
    }
    const authentication = this.authenticationLoadStorage();
    if (authentication) {
      return true;
    }
    return false;
  }

  authToken(): string {
    if (this.isLoggedIn()) {
      return this.authentication().token;
    }
    const authentication = this.authenticationLoadStorage();
    if (authentication) {
      authentication.lastAccessTime = dayjs().unix();
      this.login(authentication);
      return authentication.token;
    }
    throw new HttpErrorResponse({
      error: 'Authenticate is incorrectness,please login again.',
      status: 401,
    });
  }

  login(authentication: Authentication): void {
    this.isLoggedIn.set(true);
    this.authentication.set(authentication);
    this._storage.set(this.authenticationKey, JSON.stringify(authentication));
  }

  logout(): void {
    this.isLoggedIn.set(false);
    this.authentication.set({} as Authentication);
    this._storage.remove(this.authenticationKey);
  }

  authenticationLoadStorage(): Authentication | null {
    const authenticationJsonStr = this._storage.get(this.authenticationKey);
    if (authenticationJsonStr) {
      const authentication: Authentication = JSON.parse(authenticationJsonStr);
      const lastAccessTime = dayjs.unix(authentication.lastAccessTime);
      const diffSec = dayjs().diff(lastAccessTime, 'second');
      if (diffSec < authentication.expires) {
        return authentication;
      }
    }
    return null;
  }
}

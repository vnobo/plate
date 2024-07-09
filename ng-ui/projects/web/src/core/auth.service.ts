import { HttpErrorResponse } from '@angular/common/http';
import { inject, Injectable, signal } from '@angular/core';
import { toObservable } from '@angular/core/rxjs-interop';
import { CanActivateChildFn, CanActivateFn, CanMatchFn, Router } from '@angular/router';
import { SessionStorageService } from 'plate-commons';
import { Observable } from 'rxjs';
import dayjs from 'dayjs';

export interface Authentication {
  token: string;
  expires: number;
  lastAccessTime: Date;
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
  _storage = inject(SessionStorageService);
  private readonly authenticationKey = 'authentication';
  private isLoggedIn = signal(false);
  authenticated$: Observable<boolean> = toObservable(this.isLoggedIn);
  private authentication = signal({} as Authentication);
  authentication$: Observable<Authentication> = toObservable(this.authentication);

  isLogged(): boolean {
    const loadResult = this.authenticationLoadStorage();
    if (loadResult) {
      console.log('自动认证: {}', loadResult);
    }
    return this.isLoggedIn();
  }

  authToken(): string {
    if (this.isLoggedIn()) {
      return this.authentication().token;
    }
    const loadResult = this.authenticationLoadStorage();
    if (loadResult) {
      return this.authentication().token;
    }
    throw new HttpErrorResponse({
      error: 'Authenticate is incorrectness,please login again.',
      status: 401,
    });
  }

  login(authentication: Authentication) {
    this.isLoggedIn.set(true);
    this.authentication.set(authentication);
    this._storage.set(this.authenticationKey, JSON.stringify(authentication));
  }

  logout(): void {
    this.isLoggedIn.set(false);
    this.authentication.set({} as Authentication);
    this._storage.remove(this.authenticationKey);
  }

  private authenticationLoadStorage() {
    const authenticationJsonStr = this._storage.get(this.authenticationKey);
    if (authenticationJsonStr && authenticationJsonStr != null && authenticationJsonStr != 'null') {
      const authentication: Authentication = JSON.parse(authenticationJsonStr);
      const lastAccessTime = dayjs(authentication.lastAccessTime);
      if (dayjs().diff(lastAccessTime) > authentication.expires) {
        this.logout();
        return false;
      }
      authentication.lastAccessTime = new Date();
      this.login(authentication);
      return true;
    }
    return false;
  }
}

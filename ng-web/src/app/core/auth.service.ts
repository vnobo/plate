import { HttpErrorResponse } from '@angular/common/http';
import { inject, Injectable, signal } from '@angular/core';
import { CanActivateChildFn, CanActivateFn, CanMatchFn, Router } from '@angular/router';
import dayjs from 'dayjs';
import { SessionStorageService } from './storage/session-storage';

// 定义一个接口，用于存储用户的认证信息
export interface Authentication {
  token: string;
  expires: number;
  lastAccessTime: number;
  details: any;
}

// 定义一个函数，用于判断用户是否已登录
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
  readonly loginUrl = '/login';
  private _storage = inject(SessionStorageService);
  private readonly authenticationKey = 'authentication';
  private isLoggedIn = signal(false);
  private authentication = signal({} as Authentication);

  authenticationToken() {
    if (this.isLoggedIn()) {
      return this.authentication();
    }
    const authentication = this.authenticationLoadStorage();
    if (authentication) {
      authentication.lastAccessTime = dayjs().unix();
      this.login(authentication);
      return authentication;
    }
    return null;
  }

  isLogged(): boolean {
    if (this.isLoggedIn()) {
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

  private authenticationLoadStorage(): Authentication | null {
    const authenticationJsonStr = this._storage.get(this.authenticationKey);
    if (authenticationJsonStr) {
      const authentication: Authentication = JSON.parse(authenticationJsonStr);
      const lastAccessTime = dayjs.unix(authentication.lastAccessTime);
      const diffSec = dayjs().diff(lastAccessTime, 'second');
      if (diffSec < authentication.expires) {
        return authentication;
      }
      this._storage.remove(this.authenticationKey);
    }
    return null;
  }
}

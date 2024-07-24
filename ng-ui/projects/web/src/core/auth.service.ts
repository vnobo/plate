import { HttpErrorResponse } from '@angular/common/http';
import { inject, Injectable, signal } from '@angular/core';
import { CanActivateChildFn, CanActivateFn, CanMatchFn, Router } from '@angular/router';
import dayjs from 'dayjs';
import { SessionStorageService } from '../shared/session-storage.service';
import { RSocketCLientService } from './rsocket.service';

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
  private _socket = inject(RSocketCLientService);

  readonly loginUrl = '/auth/login';
  private _storage: SessionStorageService = inject(SessionStorageService);
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
    throw new HttpErrorResponse({
      error: 'Authenticate is incorrectness,please login again.',
      status: 401,
    });
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
    this._socket.connect(authentication.token);
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

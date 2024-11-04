import { HttpErrorResponse } from '@angular/common/http';
import { inject, Injectable, signal } from '@angular/core';
import dayjs from 'dayjs';

import { Authentication } from '../../../typings';
import { SessionStorageService } from '..';

@Injectable({ providedIn: 'root' })
export class TokenService {
  readonly loginUrl = '/login';
  public redirectUrl = '';
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

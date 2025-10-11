import { HttpErrorResponse } from '@angular/common/http';
import { afterNextRender, inject, Injectable } from '@angular/core';
import { Authentication } from '@plate/types';
import { SessionStorage } from '@app/core';
import dayjs from 'dayjs';

@Injectable({ providedIn: 'root' })
export class TokenService {
  public readonly loginUrl = '/passport/login';
  private readonly authenticationKey = 'authentication';
  private readonly _storage = inject(SessionStorage);

  public redirectUrl = '/home';
  private isLoggedIn = false;
  private authentication = {} as Authentication;

  constructor() {
    afterNextRender(() => {});
  }

  authenticationToken(): Authentication | null {
    if (this.isLoggedIn) {
      return this.authentication;
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
    return this.isLoggedIn;
  }

  authToken(): string {
    if (this.isLoggedIn) {
      return this.authentication.token;
    }
    const authentication = this.authenticationLoadStorage();
    if (authentication) {
      authentication.lastAccessTime = dayjs().unix();
      this.login(authentication);
      return authentication.token;
    }
    throw new HttpErrorResponse({
      error: 'Authentication is invalid, please log in again.',
      status: 401,
    });
  }

  login(authentication: Authentication): void {
    this.isLoggedIn = true;
    this.authentication = authentication;
    var jsonStr = JSON.stringify(authentication);
    var enstr = btoa(encodeURIComponent(jsonStr));
    this._storage.setItem(this.authenticationKey, enstr);
  }

  logout(): void {
    this.isLoggedIn = false;
    this.authentication = {} as Authentication;
    this._storage.removeItem(this.authenticationKey);
  }

  private authenticationLoadStorage(): Authentication | null {
    const authenticationJsonStr = this._storage.getItem(this.authenticationKey);
    if (authenticationJsonStr) {
      const authentication: Authentication = JSON.parse(
        decodeURIComponent(atob(authenticationJsonStr)),
      );
      const lastAccessTime = dayjs.unix(authentication.lastAccessTime);
      const diffSec = dayjs().diff(lastAccessTime, 'second');
      if (diffSec < authentication.expires) {
        return authentication;
      }
      this._storage.removeItem(this.authenticationKey);
    }
    return null;
  }
}

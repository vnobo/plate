import {HttpErrorResponse} from '@angular/common/http';
import {inject, Injectable} from '@angular/core';
import {SessionStorageService} from '@app/core';
import {Authentication} from '@app/core/types';
import dayjs from 'dayjs';

@Injectable({ providedIn: 'root' })
export class TokenService {
  public readonly loginUrl = '/passport/login';
  private readonly authenticationKey = 'authentication';
  private readonly _storage = inject(SessionStorageService);
  public redirectUrl = '';
  private isLoggedIn = false;
  private authentication = {} as Authentication;

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
      error: 'Authenticate is incorrectness,please login again.',
      status: 401,
    });
  }

  login(authentication: Authentication): void {
    this.isLoggedIn = true;
    this.authentication = authentication;
    this._storage.set(this.authenticationKey, JSON.stringify(authentication));
  }

  logout(): void {
    this.isLoggedIn = false;
    this.authentication = {} as Authentication;
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

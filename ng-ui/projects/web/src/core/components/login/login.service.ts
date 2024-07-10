import { HttpClient, HttpHeaders } from '@angular/common/http';
import { inject, Injectable, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { Authentication, AuthService } from '../../auth.service';
import { BrowserStorageService } from '../../services/browser-storage.service';
import dayjs from 'dayjs';

export interface Credentials {
  password: string | null | undefined;
  username: string | null | undefined;
}

@Injectable({
  providedIn: 'root',
})
export class LoginService {
  _http = inject(HttpClient);
  _storage = inject(BrowserStorageService);
  _auth = inject(AuthService);
  private readonly storageKey = 'credentials';
  private credentials = signal({} as Credentials);

  autoLogin(): Authentication | null {
    const authentication = this._auth.authenticationLoadStorage();
    if (authentication) {
      authentication.lastAccessTime = dayjs().unix();
      this._auth.login(authentication);
      return authentication;
    }
    return null;
  }

  login(credentials: Credentials): Observable<Authentication> {
    const headers: HttpHeaders = new HttpHeaders(
      credentials
        ? {
            authorization: 'Basic ' + btoa(credentials.username + ':' + credentials.password),
          }
        : {}
    );
    return this._http
      .get<Authentication>('/oauth2/token', { headers: headers })
      .pipe(tap(authentication => this._auth.login(authentication)));
  }

  setRememberMe(credentials: Credentials) {
    let creStr = JSON.stringify(credentials);
    this._storage.set(this.storageKey, creStr);
    this.credentials.set(credentials);
  }

  getRememberMe() {
    let creStr = this._storage.get(this.storageKey);
    if (creStr) {
      this.credentials.set(JSON.parse(creStr));
    }
    return this.credentials();
  }

  logout() {
    this._auth.logout();
    this._storage.remove(this.storageKey);
    this.credentials.set({} as Credentials);
  }
}

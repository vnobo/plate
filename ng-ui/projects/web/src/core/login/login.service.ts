import { HttpClient, HttpHeaders } from '@angular/common/http';
import { inject, Injectable, signal } from '@angular/core';
import { BrowserStorageService } from 'plate-commons';
import { tap } from 'rxjs';
import { Authentication, AuthService } from '../auth.service';

export interface Credentials {
  password: string | null | undefined;
  username: string | null | undefined;
}

@Injectable({
  providedIn: 'root',
})
export class LoginService {
  private readonly storageKey = 'credentials';
  _http = inject(HttpClient);
  _storage = inject(BrowserStorageService);
  _auth = inject(AuthService);
  private credentials = signal({} as Credentials);

  login(credentials: Credentials) {
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
    creStr = btoa(creStr);
    this._storage.set(this.storageKey, creStr);
    this.credentials.set(credentials);
  }

  getRememberMe() {
    let creStr = this._storage.get(this.storageKey);
    if (creStr) {
      creStr = atob(creStr);
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

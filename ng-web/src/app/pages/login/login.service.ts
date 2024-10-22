import { HttpClient, HttpHeaders } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable, tap } from 'rxjs';
import dayjs from 'dayjs';
import { Authentication, AuthService } from '../../core/auth.service';
import { BrowserStorage } from '../../core/storage/browser-storage';

export interface Credentials {
  password: string | null | undefined;
  username: string | null | undefined;
}

@Injectable({
  providedIn: 'root',
})
export class LoginService {
  private _http = inject(HttpClient);
  private _storage = inject(BrowserStorage);
  private readonly storageKey = 'credentials';

  _auth = inject(AuthService);

  autoLogin(): Authentication | null {
    const authentication = this._auth.authenticationToken();
    if (authentication && Object.keys(authentication).length !== 0) {
      var lastAccessTime = dayjs.unix(authentication.lastAccessTime);
      if (dayjs().diff(lastAccessTime, 'seconds') > authentication.expires) {
        return null; // token expired
      }
      authentication.lastAccessTime = dayjs().unix();
      this._auth.login(authentication);
      return authentication;
    }
    return null;
  }

  login(credentials: Credentials): Observable<Authentication> {
    const headers: HttpHeaders = new HttpHeaders({
      authorization: 'Basic ' + btoa(credentials.username + ':' + credentials.password),
    });
    return this._http.get<Authentication>('/oauth2/login', { headers: headers }).pipe(
      tap(authentication => {
        this._auth.login(authentication);
      })
    );
  }

  setRememberMe(credentials: Credentials) {
    let creStr = JSON.stringify(credentials);
    this._storage.set(this.storageKey, creStr);
  }

  getRememberMe(): Credentials | null {
    let creStr = this._storage.get(this.storageKey);
    if (creStr && Object.keys(creStr).length !== 0) {
      return JSON.parse(creStr);
    }
    return null;
  }

  logout() {
    this._http.get('/oauth2/logout').subscribe(res => {
      this._auth.logout();
      this._storage.remove(this.storageKey);
    });
  }
}

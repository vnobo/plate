import {HttpClient, HttpHeaders} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable, tap, throwError} from 'rxjs';
import {AuthService} from '../../core/auth.service';
import {BrowserStorageService} from 'plate-commons';

export interface Authentication {
  token: string;
  expires: number;
  lastAccessTime: Date;
}

export interface Credentials {
  password: string | null | undefined;
  username: string | null | undefined;
}

@Injectable({
  providedIn: 'root',
})
export class LoginService {
  private readonly storageKey = 'credentials';

  private credentials: Credentials | null | undefined = null;

  constructor(
    private http: HttpClient,
    private auth: AuthService,
    private storage: BrowserStorageService
  ) {
  }

  login(credentials: Credentials): Observable<Authentication> {
    if (
      credentials.username == undefined &&
      credentials.password == undefined
    ) {
      return throwError(() => '用户名和密码不能为[undefined]!');
    }
    const headers: HttpHeaders = new HttpHeaders(
      credentials
        ? {
          authorization:
            'Basic ' +
            btoa(credentials.username + ':' + credentials.password),
        }
        : {}
    );
    return this.http
      .get<Authentication>('/oauth2/token', {headers: headers})
      .pipe(tap(authentication => this.auth.login(authentication.token)));
  }

  setRememberMe(credentials: Credentials) {
    let creStr = JSON.stringify(credentials);
    creStr = btoa(creStr);
    this.storage.set(this.storageKey, creStr);
    this.credentials = credentials;
  }

  getRememberMe() {
    let creStr = this.storage.get(this.storageKey);
    if (creStr) {
      console.log(creStr);
      creStr = atob(creStr);
      this.credentials = JSON.parse(creStr);
    }
    return this.credentials;
  }

  logout() {
    this.auth.logout();
    this.storage.remove(this.storageKey);
    this.credentials = null;
  }
}

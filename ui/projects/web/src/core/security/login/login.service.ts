import {HttpClient, HttpHeaders} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {BrowserStorageService} from '@plate/commons';
import {Observable, tap} from 'rxjs';
import {AuthService} from '../../../core/auth.service';

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
      .pipe(tap((authentication) => this.auth.login(authentication.token)));
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

  /**
   * 登出功能实现。
   * 该方法负责协调登录状态的解除和相关登录信息的清除工作。
   * 它调用认证模块的登出方法来解除用户认证状态，然后从存储中移除登录凭证，
   * 最后将本地持有的凭证信息置为null，表示登出成功。
   */
  logout() {
    // 调用认证模块的登出方法，解除用户认证状态
    this.auth.logout();
    // 从存储中移除登录凭证，确保凭证信息不被非法访问
    this.storage.remove(this.storageKey);
    // 将本地持有的凭证信息置为null，表示当前用户已登出
    this.credentials = null;
  }
}

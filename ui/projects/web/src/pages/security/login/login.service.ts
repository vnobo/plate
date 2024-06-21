import {Inject, Injectable, PLATFORM_ID,} from '@angular/core';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {Observable, tap} from 'rxjs';
import {AuthService} from '../../../core/auth.service';
import {isPlatformBrowser} from '@angular/common';

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
  private afterNextRender: boolean = false;
  private credentials: Credentials | null | undefined = null;

  constructor(
    private http: HttpClient,
    private auth: AuthService,
    @Inject(PLATFORM_ID) private platformId: Object
  ) {
    if (isPlatformBrowser(this.platformId)) {
      this.afterNextRender = true;
    }
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
    if (this.afterNextRender) {
      let creStr = JSON.stringify(credentials);
      creStr = btoa(creStr);
      sessionStorage.setItem('credentials', creStr);
    }
    this.credentials = credentials;
  }

  getRememberMe() {
    if (this.afterNextRender) {
      let creStr = sessionStorage.getItem('credentials');
      if (creStr) {
        creStr = atob(creStr);
        this.credentials = JSON.parse(creStr);
      }
    }
    return this.credentials;
  }

  logout() {
    this.auth.logout();
    if (this.afterNextRender) {
      sessionStorage.removeItem('credentials');
    }
    this.credentials = null;
  }
}

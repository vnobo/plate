import {afterNextRender, Injectable} from '@angular/core';
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {Observable, tap} from "rxjs";
import {AuthService} from "../../../core/auth.service";

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
  providedIn: 'root'
})
export class LoginService {

  constructor(private http: HttpClient, private auth: AuthService) {
  }

  login(credentials: Credentials): Observable<Authentication> {
    const headers: HttpHeaders = new HttpHeaders(credentials ? {
      authorization: 'Basic ' + btoa(credentials.username + ':' + credentials.password)
    } : {});
    return this.http.get<Authentication>('/oauth2/token', {headers: headers})
      .pipe(tap(authentication => this.auth.login(authentication.token)));
  }

  rememberMe(credentials: Credentials) {
    //todo save credentials in local storage
    afterNextRender(() => {
      // Safe to check `scrollHeight` because this will only run in the browser, not the server.
    });
  }

  logout() {
    //todo remove credentials from local storage
    this.auth.logout();
    afterNextRender(() => {
      // Safe to check `scrollHeight` because this will only run in the browser, not the server.
    });
  }
}

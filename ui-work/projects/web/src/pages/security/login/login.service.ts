import {Injectable} from '@angular/core';
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

  rememberMe(credentials: Credentials): boolean {
    //localStorage.setItem('rememberMe', 'true');
    //localStorage.setItem('credentials', JSON.stringify(credentials));
    return true;
  }

  getRememberMe(): boolean {
    return false;
  }

  clearRememberMe(): void {
    //localStorage.removeItem('rememberMe');
    // localStorage.removeItem('credentials');
  }

}

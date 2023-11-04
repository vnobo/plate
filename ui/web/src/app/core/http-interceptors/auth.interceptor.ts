import {Injectable} from '@angular/core';
import {HttpEvent, HttpHandler, HttpInterceptor, HttpRequest} from '@angular/common/http';
import {Observable} from 'rxjs';
import {AuthService} from "../auth.service";

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  constructor(private authService: AuthService) {
  }

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    if (!this.authService.isLoggedIn) {
      return next.handle(request);
    }
    const authReq = request.clone(
      {headers: request.headers.set('x-auth-token', this.authService.authToken())}
    );
    return next.handle(authReq);
  }

}

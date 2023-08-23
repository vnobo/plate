import {Injectable} from '@angular/core';
import {HttpEvent, HttpHandler, HttpInterceptor, HttpRequest} from '@angular/common/http';
import {Observable} from 'rxjs';
import {ActivatedRoute, Router} from "@angular/router";
import {AuthService} from "../security/auth.service";

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  constructor(private router: Router,
              private route: ActivatedRoute,
              private authService: AuthService) {
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

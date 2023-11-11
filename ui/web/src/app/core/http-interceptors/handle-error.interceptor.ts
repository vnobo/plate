import {Injectable} from '@angular/core';
import {HttpErrorResponse, HttpEvent, HttpHandler, HttpInterceptor, HttpRequest} from '@angular/common/http';
import {catchError, Observable, throwError, timeout} from 'rxjs';
import {Router} from "@angular/router";
import {AuthService} from "../auth.service";
import {NzMessageService} from "ng-zorro-antd/message";

@Injectable()
export class HandleErrorInterceptor implements HttpInterceptor {
  constructor(private router: Router,
              private authService: AuthService,
              private _message: NzMessageService) {
  }

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    return next.handle(request).pipe(timeout({first: 50_000, each: 100_000}),
      catchError(err => this.handleError(err)));
  }

  private handleError(errorResponse: HttpErrorResponse) {

    if (errorResponse.error.message) {
      this._message.error(errorResponse.error.message);
      return throwError(() => errorResponse.error.message);
    }

    if (errorResponse.status === 401) {
      this.authService.logout();
      this.router.navigate([this.authService.loginUrl]).then();
      return throwError(() => $localize`:@@errorMessage401:Authenticate is noniff ,please login again.`);
    } else if (errorResponse.status === 407) {
      this.authService.logout();
      this.router.navigate([this.authService.loginUrl]).then();
      return throwError(() => $localize`:@@errorMessage407:Authenticate is incorrectness,please login again.`);
    } else if (errorResponse.status === 403) {
      this.authService.logout();
      this.router.navigate([this.authService.loginUrl]).then();
      return throwError(() => $localize`:@@errorMessage403:Captcha Token is incorrectness,please login again.`);
    }
    console.error($localize`:@@errorMessage:Backend returned code ${errorResponse.status}, body was: `, errorResponse.error);
    // return an observable with a user-facing error message
    return throwError(() => errorResponse);
  }
}

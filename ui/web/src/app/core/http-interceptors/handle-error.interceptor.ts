import {Injectable} from '@angular/core';
import {HttpErrorResponse, HttpEvent, HttpHandler, HttpInterceptor, HttpRequest} from '@angular/common/http';
import {catchError, Observable, throwError, timeout} from 'rxjs';
import {MatSnackBar} from "@angular/material/snack-bar";
import {Router} from "@angular/router";
import {AuthService} from "../auth.service";

@Injectable()
export class HandleErrorInterceptor implements HttpInterceptor {
  constructor(private router: Router,
              private authService: AuthService,
              private _snackBar: MatSnackBar) {
  }

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    return next.handle(request).pipe(timeout({first: 50_000, each: 100_000}),
      catchError(err => this.handleError(err)));
  }

  private handleError(errorResponse: HttpErrorResponse) {

    this._snackBar.open(errorResponse.error.message, $localize`:@@snackBarAction:Close`, {
      duration: 3000, verticalPosition: 'top', horizontalPosition: 'center'
    });

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

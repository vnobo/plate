import {Injectable} from '@angular/core';
import {HttpErrorResponse, HttpEvent, HttpHandler, HttpInterceptor, HttpRequest} from '@angular/common/http';
import {catchError, Observable, throwError, timeout} from 'rxjs';
import {ActivatedRoute, Router} from "@angular/router";
import {AuthService} from "../auth.service";
import {MatSnackBar} from "@angular/material/snack-bar";

@Injectable()
export class HandleErrorInterceptor implements HttpInterceptor {
  constructor(private router: Router,
              private route: ActivatedRoute,
              private authService: AuthService,
              private _snackBar: MatSnackBar) {
  }

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    return next.handle(request).pipe(timeout({each: 10000}),
      catchError(err => this.handleError(err)));
  }

  private handleError(error: HttpErrorResponse) {

    this._snackBar.open(error.error.msg, $localize`:@@snackBarAction:Close`, {
      duration: 3000, verticalPosition: 'top', horizontalPosition: 'center'
    });

    if (error.status === 401) {
      this.authService.logout();
      this.router.navigate([this.authService.loginUrl]).then();
      return throwError(() => $localize`:@@errorMessage401:Authenticate is noniff ,please login again.`);
    } else if (error.status === 407) {
      this.authService.logout();
      this.router.navigate([this.authService.loginUrl]).then();
      return throwError(() => $localize`:@@errorMessage407:Authenticate is incorrectness,please login again.`);
    } else if (error.status === 403) {
      this.authService.logout();
      this.router.navigate([this.authService.loginUrl]).then();
      return throwError(() => $localize`:@@errorMessage403:Captcha Token is incorrectness,please login again.`);
    }
    console.error($localize`:@@errorMessage:Backend returned code ${error.status}, body was: `, error.error);
    // return an observable with a user-facing error message
    return throwError(() => error);
  }
}

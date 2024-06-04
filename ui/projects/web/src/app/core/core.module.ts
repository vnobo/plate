import {inject, NgModule, Optional, SkipSelf} from '@angular/core';
import {
  HttpEvent,
  HttpHandlerFn,
  HttpRequest,
  provideHttpClient,
  withFetch,
  withInterceptors,
  withInterceptorsFromDi,
  withXsrfConfiguration
} from "@angular/common/http";
import {catchError, finalize, Observable, throwError, timeout} from "rxjs";
import {AuthService} from "./auth.service";
import {LoadingService} from "./loading.service";
import {MessageService} from "../shared/message.service";
import {Router} from "@angular/router";
import {environment} from "../../environments/environment";

export function defaultInterceptor(req: HttpRequest<unknown>, next: HttpHandlerFn): Observable<HttpEvent<unknown>> {

  const _loading = inject(LoadingService);
  const _message = inject(MessageService);

  _loading.show();
  if (req.url.indexOf('assets/') > -1) {
    return next(req);
  }
  const originalUrl = req.url.indexOf('http') > -1 ? req.url : environment.host + req.url;
  const xRequestedReq = req.clone({
    headers: req.headers.append('X-Requested-With', 'XMLHttpRequest'),
    url: originalUrl
  });
  return next(xRequestedReq).pipe(timeout({first: 5_000, each: 10_000}),
    catchError(errorResponse => {
      if (errorResponse.error.message) {
        _message.error(errorResponse.error.message);
        return throwError(() => errorResponse.error.message);
      }
      console.error($localize`:@@errorMessage:Backend returned code ${errorResponse.status},
       body was: ${errorResponse.message}`);
      return throwError(() => errorResponse);
    }), finalize(() => _loading.hide()));
}

export function authTokenInterceptor(req: HttpRequest<unknown>, next: HttpHandlerFn): Observable<HttpEvent<unknown>> {
  const _auth = inject(AuthService);
  const _route = inject(Router);

  if (!_auth.isLoggedIn) {
    return next(req);
  }

  const authReq = req.clone({
    headers: req.headers.set('Authorization', `Bearer ${_auth.authToken()}`),
  });

  return next(authReq).pipe(catchError(errorResponse => {
    if (errorResponse.status === 401) {
      _auth.logout();
      _route.navigate([_auth.loginUrl]).then();
      return throwError(() => $localize`:@@errorMessage401:身份验证无效，请重新登录。`);
    } else if (errorResponse.status === 407) {
      _auth.logout();
      _route.navigate([_auth.loginUrl]).then();
      return throwError(() => $localize`:@@errorMessage407:认证不正确，请重新登录。`);
    } else if (errorResponse.status === 403) {
      _auth.logout();
      _route.navigate([_auth.loginUrl]).then();
      return throwError(() => $localize`:@@errorMessage403:验证码令牌错误，请重新登录。`);
    } else {
      console.error(`Backend returned authToken code ${errorResponse.status}, body was: `, errorResponse.error);
      return throwError(() => errorResponse);
    }
  }));
}


@NgModule({
  exports: [], imports: [], providers: [
    provideHttpClient(withFetch(), withInterceptors([defaultInterceptor, authTokenInterceptor])),
    provideHttpClient(withInterceptorsFromDi(), withXsrfConfiguration({
      cookieName: 'XSRF-TOKEN',
      headerName: 'X-XSRF-TOKEN'
    }))
  ]
})
export class CoreModule {
  constructor(@Optional() @SkipSelf() parentModule?: CoreModule) {
    if (parentModule) {
      throw new Error(
        'CoreModule is already loaded. Import it in the AppModule only');
    }
  }
}

import {inject, NgModule, Optional, SkipSelf} from '@angular/core';
import {
  HttpClientModule,
  HttpClientXsrfModule, HttpEvent, HttpHandlerFn, HttpRequest,
  provideHttpClient,
  withFetch,
  withInterceptors
} from "@angular/common/http";
import {catchError, Observable, throwError, timeout} from "rxjs";
import {AuthService} from "./auth.service";
import {LoadingService} from "./loading.service";
import {MessageService} from "../shared/message.service";
import {Router} from "@angular/router";
import {environment} from "../../environments/environment";

export function defaultInterceptor(req: HttpRequest<unknown>, next: HttpHandlerFn): Observable<HttpEvent<unknown>> {
  const _auth = inject(AuthService);
  const _loading = inject(LoadingService);
  const _message = inject(MessageService);
  const _route = inject(Router);

  _loading.show();
  if (req.url.indexOf('assets/') > -1 || !_auth.isLoggedIn) {
    return next(req);
  }
  const originalUrl = req.url.indexOf('http') > -1 ? req.url : environment.host + req.url;
  const authReq = req.clone({
    headers: req.headers.append('X-Requested-With', 'XMLHttpRequest')
      .append('x-auth-token', _auth.authToken()),
    url: originalUrl
  });
  return next(authReq).pipe(timeout({first: 50_000, each: 100_000}),
    catchError(errorResponse => {
      if (errorResponse.error.message) {
        _message.error(errorResponse.error.message);
        return throwError(() => errorResponse.error.message);
      }

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
      }
      console.error($localize`:@@errorMessage:Backend returned code ${errorResponse.status}, body was: `, errorResponse.error);
      return throwError(() => errorResponse);
    }));
}


@NgModule({
  imports: [HttpClientModule,
    HttpClientXsrfModule.withOptions({
      cookieName: 'XSRF-TOKEN',
      headerName: 'X-XSRF-TOKEN'
    })],
  exports: [
    HttpClientModule,
    HttpClientXsrfModule
  ],
  providers: [
    provideHttpClient(
      withFetch(),
      withInterceptors([defaultInterceptor])
    )
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

import { inject } from '@angular/core';
import { HttpEvent, HttpHandlerFn, HttpRequest } from '@angular/common/http';
import { catchError, finalize, Observable, throwError, timeout } from 'rxjs';
import { Router } from '@angular/router';
import { environment } from '../../environments/environment';
import { ProgressBar } from './progress-bar';
import { AuthService } from './auth.service';
import { NzMessageService } from 'ng-zorro-antd/message';

export function defaultInterceptor(
  req: HttpRequest<unknown>,
  next: HttpHandlerFn,
): Observable<HttpEvent<unknown>> {
  const _loading = inject(ProgressBar);
  const _message = inject(NzMessageService);
  _loading.show();
  if (req.url.indexOf('assets/') > -1) {
    return next(req);
  }
  const originalUrl = req.url.indexOf('http') > -1 ? req.url : environment.host + req.url;
  const xRequestedReq = req.clone({
    headers: req.headers.append('X-Requested-With', 'XMLHttpRequest'),
    url: originalUrl,
  });
  return next(xRequestedReq).pipe(
    timeout({ first: 5_000, each: 10_000 }),
    catchError(errorResponse => {
      let alertMessage = '';
      const status = errorResponse.status;
      if (status > 0) {
        if (errorResponse.error) {
          alertMessage = errorResponse.error.message;
        } else {
          alertMessage = '服务器无响应,请稍后重试!';
        }
      } else {
        alertMessage = errorResponse.message;
      }
      _message.error(alertMessage);
      return throwError(() => errorResponse);
    }),
    finalize(() => _loading.hide()),
  );
}

export function authTokenInterceptor(
  req: HttpRequest<unknown>,
  next: HttpHandlerFn,
): Observable<HttpEvent<unknown>> {
  const _auth = inject(AuthService);
  const _route = inject(Router);

  if (!_auth.isLogged()) {
    return next(req);
  }
  const newReq = req.clone({
    headers: req.headers.set('Authorization', `Bearer ${_auth.authToken()}`),
  });

  return next(newReq).pipe(
    catchError(errorResponse => {
      if (errorResponse.status === 401) {
        _auth.logout();
        _route.navigate([_auth.loginUrl]).then();
      }
      return throwError(() => errorResponse);
    })
  );
}

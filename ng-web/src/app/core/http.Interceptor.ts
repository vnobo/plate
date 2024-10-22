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
    finalize(() => _loading.hide()),
  );
}

export function handleErrorInterceptor(req: HttpRequest<unknown>, next: HttpHandlerFn) {
  const _message = inject(NzMessageService);
  const _auth = inject(AuthService);
  const _route = inject(Router);
  return next(req).pipe(
    catchError(error => {
      if (error.status === 0) {
        // A client-side or network error occurred. Handle it accordingly.
        console.error('An error occurred:', error.error);
        _message.error(`网络错误,请检查网络连接后重试!`);
      } else {
        if (error.status === 401) {
          if (error.error.path === '/oauth2/login') {
            return throwError(() => error.error);
          }
          _auth.logout();
          _route.navigate([_auth.loginUrl]).then();
          _message.error(`访问认证失败,错误码: ${error.status},详细信息: ${error.error.message}`);
          return throwError(() => error.error);
        } else {
          _message.error(
            `服务器访问错误,请稍后重试. 错误码: ${error.status},详细信息: ${error.error}`,
          );
        }
      }
      console.error(`Backend returned code ${error.status}, body was: `, error.error);
      return throwError(() => error);
    }),
  );
}
export function authTokenInterceptor(
  req: HttpRequest<unknown>,
  next: HttpHandlerFn,
): Observable<HttpEvent<unknown>> {
  const _auth = inject(AuthService);
  if (!_auth.isLogged()) {
    return next(req);
  }
  const newReq = req.clone({
    headers: req.headers.set('Authorization', `Bearer ${_auth.authToken()}`),
  });
  return next(newReq);
}

export const indexInterceptor = [defaultInterceptor, handleErrorInterceptor, authTokenInterceptor];

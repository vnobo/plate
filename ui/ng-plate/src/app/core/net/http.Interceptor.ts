import { inject } from '@angular/core';
import { HttpEvent, HttpHandlerFn, HttpRequest } from '@angular/common/http';
import { catchError, finalize, Observable, throwError, timeout } from 'rxjs';
import { Router } from '@angular/router';

import { TokenService } from '../services/token.service';
import { ProgressService } from '@app/plugins/progress';
import { environment } from '@envs/env';

function defaultInterceptor(
  req: HttpRequest<unknown>,
  next: HttpHandlerFn,
): Observable<HttpEvent<unknown>> {
  const _loading = inject(ProgressService);
  _loading.show();
  if (req.url.indexOf('assets/') > -1) {
    return next(req);
  }
  const originalUrl = req.url.indexOf('http') > -1 ? req.url : environment.host + req.url;
  const xRequestedReq = req.clone({ url: originalUrl });
  return next(xRequestedReq).pipe(finalize(() => _loading.hide()));
}

function timeoutInterceptor(req: HttpRequest<unknown>, next: HttpHandlerFn) {
  return next(req).pipe(timeout({ first: 5_000, each: 10_000 }));
}

function authTokenInterceptor(req: HttpRequest<unknown>, next: HttpHandlerFn) {
  const _auth = inject(TokenService);
  // 只给自家 API 带令牌：绝对 URL 可能是第三方，挂上 Bearer 等于把令牌交出去
  const isOwnApi = environment.host
    ? req.url.startsWith(environment.host)
    : req.url.indexOf('http') !== 0;
  // 只跳过登录端点本身：子串匹配 /login 会让 /login-history 这类接口静默丢令牌
  if (!isOwnApi || req.url.indexOf('/oauth2/login') > -1) {
    return next(req);
  }
  const newReq = req.clone({
    // 用 set 而不是 append：调用方若自带 Authorization，append 会叠成两个值
    headers: req.headers.set('Authorization', `Bearer ${_auth.authToken()}`),
  });
  return next(newReq);
}

function handleErrorInterceptor(req: HttpRequest<unknown>, next: HttpHandlerFn) {
  const _auth = inject(TokenService);
  const _route = inject(Router);
  return next(req).pipe(
    catchError((error) => {
      if (error.status === 401) {
        if (
          error.error &&
          typeof error.error === 'object' &&
          error.error.path === '/oauth2/login'
        ) {
          return throwError(() => error.error);
        }
        // 令牌已失效：不清掉旧令牌，guard 会凭 sessionStorage 里的它再次放行，
        // 形成「进内页 → 401 → 回登录页」的循环
        _auth.logout();
        _route.navigate([_auth.loginUrl]).then();
      }
      return throwError(() => error);
    }),
  );
}

function apiVersionInterceptor(
  req: HttpRequest<unknown>,
  next: HttpHandlerFn,
): Observable<HttpEvent<unknown>> {
  const newReq = req.clone({
    headers: req.headers.append('x-api-version', `v1`),
  });
  return next(newReq);
}

export const indexInterceptor = [
  defaultInterceptor,
  apiVersionInterceptor,
  authTokenInterceptor,
  handleErrorInterceptor,
  timeoutInterceptor,
];

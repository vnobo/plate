import { inject } from '@angular/core';
import { HttpEvent, HttpHandlerFn, HttpRequest } from '@angular/common/http';
import { catchError, finalize, Observable, throwError, timeout } from 'rxjs';
import { Router } from '@angular/router';

import { ProgressBar, TokenService } from '@app/core';
import { environment } from '@envs/env';

function defaultInterceptor(
  req: HttpRequest<unknown>,
  next: HttpHandlerFn,
): Observable<HttpEvent<unknown>> {
  const _loading = inject(ProgressBar);
  _loading.show();
  if (req.url.indexOf('assets/') > -1) {
    return next(req);
  }
  const originalUrl = req.url.indexOf('http') > -1 ? req.url : environment.host + req.url;
  const xRequestedReq = req.clone({ url: originalUrl });
  return next(xRequestedReq).pipe(
    timeout({ first: 5_000, each: 10_000 }),
    finalize(() => _loading.hide()),
  );
}

function handleErrorInterceptor(req: HttpRequest<unknown>, next: HttpHandlerFn) {
  const _auth = inject(TokenService);
  const _route = inject(Router);
  return next(req).pipe(
    catchError(error => {
      if (error.status === 401) {
        if (
          error.error &&
          typeof error.error === 'object' &&
          error.error.path === '/oauth2/login'
        ) {
          return throwError(() => error.error);
        }
        //_auth.logout();
        _route.navigate([_auth.loginUrl]).then();
      }
      console.error(`Backend returned code ${error.status}, body was: `, JSON.stringify(error));
      return throwError(() => error);
    }),
  );
}

function authTokenInterceptor(
  req: HttpRequest<unknown>,
  next: HttpHandlerFn,
): Observable<HttpEvent<unknown>> {
  const _auth = inject(TokenService);
  if (!_auth.isLogged()) {
    return next(req);
  }
  const newReq = req.clone({
    headers: req.headers.set('Authorization', `Bearer ${_auth.authToken()}`),
  });
  return next(newReq);
}

export const indexInterceptor = [defaultInterceptor, handleErrorInterceptor];

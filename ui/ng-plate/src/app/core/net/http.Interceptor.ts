import {inject} from '@angular/core';
import {HttpEvent, HttpHandlerFn, HttpRequest} from '@angular/common/http';
import {catchError, Observable, throwError, timeout} from 'rxjs';
import {Router} from '@angular/router';

import {TokenService} from '@app/core';
import {environment} from '@envs/env';

function defaultInterceptor(
  req: HttpRequest<unknown>,
  next: HttpHandlerFn,
): Observable<HttpEvent<unknown>> {
  // Prefix relative URLs with the API host so they hit the backend in dev (through the proxy).
  // Absolute URLs (third-party) are left untouched.
  const originalUrl = req.url.startsWith('http') ? req.url : environment.host + req.url;
  // Mark first-party API requests as AJAX so the backend carries the session id in the
  // X-Auth-Token header instead of a cookie (see backend SessionConfiguration dual-transport).
  // Third-party requests are left untouched so we never leak that we are an AJAX client.
  const isOwnApi = environment.host
    ? originalUrl.startsWith(environment.host)
    : !req.url.startsWith('http');
  const headers = isOwnApi
    ? req.headers.set('X-Requested-With', 'XMLHttpRequest')
    : req.headers;
  const xRequestedReq = req.clone({ url: originalUrl, headers });
  return next(xRequestedReq);
}

function timeoutInterceptor(req: HttpRequest<unknown>, next: HttpHandlerFn) {
  return next(req).pipe(timeout({ first: 5_000, each: 10_000 }));
}

function authTokenInterceptor(req: HttpRequest<unknown>, next: HttpHandlerFn) {
  const _auth = inject(TokenService);
  // Only attach the session token to first-party APIs: an absolute URL may be third-party,
  // and sending our token there would leak it.
  const isOwnApi = environment.host
    ? req.url.startsWith(environment.host)
    : !req.url.startsWith('http');
  // Skip only the login endpoint itself: a substring match on /login would silently
  // drop the token from endpoints like /login-history.
  if (!isOwnApi || req.url.indexOf('/oauth2/login') > -1) {
    return next(req);
  }
  const newReq = req.clone({
    // Use set, not append: if the caller supplied its own X-Auth-Token, append would stack two values.
    // The backend resolves the session from this header on AJAX requests (X-Requested-With set above).
    headers: req.headers.set('X-Auth-Token', _auth.authToken()),
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
        // Token no longer valid: clear it so the guard will not let the stale session through
        // again and create a "enter inner page -> 401 -> back to login" loop.
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
    headers: req.headers.set('x-api-version', `v1`),
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

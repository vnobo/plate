import {inject} from '@angular/core';
import {HttpEvent, HttpHandlerFn, HttpRequest} from '@angular/common/http';
import {catchError, finalize, Observable, throwError, timeout} from 'rxjs';
import {Router} from '@angular/router';
import {environment} from '@environment/environment';
import {NzMessageService} from 'ng-zorro-antd/message';

import {ProgressBar} from '@app/core/services/progress-bar';
import {TokenService} from '@app/core/services/token.service';

function defaultInterceptor(req: HttpRequest<unknown>, next: HttpHandlerFn): Observable<HttpEvent<unknown>> {
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

function handleErrorInterceptor(req: HttpRequest<unknown>, next: HttpHandlerFn) {
  const _message = inject(NzMessageService);
  const _auth = inject(TokenService);
  const _route = inject(Router);
  return next(req).pipe(
    catchError(error => {
      if (error.status === 401) {
        if (error.error.path === '/oauth2/login') {
          return throwError(() => error.error);
        }
        _auth.logout();
        _route.navigate([_auth.loginUrl]).then();
        _message.error(`访问认证失败,错误码: ${error.status},详细信息: ${error.message}`);
        return throwError(() => error.error);
      }
      _message.error(`服务器访问错误,请稍后重试. 错误码: ${error.status},详细信息: ${error.message}`);
      console.error(`Backend returned code ${error.status}, body was: `, JSON.stringify(error));
      return throwError(() => error);
    }),
  );
}

/**
 * 认证令牌拦截器
 * 在发送 HTTP 请求前，检查用户是否已登录。如果已登录，则在请求头中添加认证令牌。
 * @param req - 原始 HTTP 请求
 * @param next - 下一个处理程序函数
 * @returns 返回一个 Observable，包含修改后的 HTTP 事件
 */
function authTokenInterceptor(req: HttpRequest<unknown>, next: HttpHandlerFn): Observable<HttpEvent<unknown>> {
  const _auth = inject(TokenService);
  if (!_auth.isLogged()) {
    return next(req);
  }
  const newReq = req.clone({
    headers: req.headers.set('Authorization', `Bearer ${_auth.authToken()}`),
  });
  return next(newReq);
}

export const indexInterceptor = [defaultInterceptor, handleErrorInterceptor, authTokenInterceptor];

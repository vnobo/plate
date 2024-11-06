import { inject } from '@angular/core';
import { HttpEvent, HttpHandlerFn, HttpRequest } from '@angular/common/http';
import { catchError, finalize, Observable, throwError, timeout } from 'rxjs';
import { Router } from '@angular/router';
import { environment } from '../../../environments/environment';
import { ProgressBar } from '../services/progress-bar';
import { NzMessageService } from 'ng-zorro-antd/message';
import { TokenService } from '../services/token.service';

/**
 * 默认的HTTP拦截器，用于处理所有HTTP请求。
 * 在请求发送前显示加载进度条，并在请求完成后隐藏。
 * 如果请求的URL包含'assets/'，则直接传递请求，不做任何修改。
 * 对于其他请求，会添加'X-Requested-With'头部，并根据环境配置拼接完整的URL。
 * 设置请求超时时间为5秒，如果请求在5秒内未完成，则触发超时处理。
 * 使用finalize操作符确保加载进度条在请求完成后隐藏。
 * @param req - HTTP请求对象
 * @param next - 下一个HTTP处理函数
 * @returns Observable<HttpEvent<unknown>> - HTTP响应事件的Observable对象
 */
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

/**
 * 错误处理拦截器，用于处理 HTTP 请求过程中可能出现的错误。
 * @param req - HTTP请求对象。
 * @param next - 下一个HTTP处理函数。
 * @returns 返回一个Observable，包含处理后的HTTP响应或错误信息。
 * 错误处理包括：
 * - 网络错误（状态码为0）：提示用户检查网络连接。
 * - 认证失败（状态码为401）：登出用户并重定向到登录页面。
 * - 其他服务器错误：提示用户稍后重试，并显示错误码和详细信息。
 */
function handleErrorInterceptor(req: HttpRequest<unknown>, next: HttpHandlerFn) {
  const _message = inject(NzMessageService);
  const _auth = inject(TokenService);
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
          _message.error(`服务器访问错误,请稍后重试. 错误码: ${error.status},详细信息: ${error.error}`);
        }
      }
      console.error(`Backend returned code ${error.status}, body was: `, error.error);
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

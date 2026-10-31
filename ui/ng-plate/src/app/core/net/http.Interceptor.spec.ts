import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { environment } from '@envs/env';
import { indexInterceptor } from './http.Interceptor';

describe('indexInterceptor 的令牌处理', () => {
  let http: HttpClient;
  let httpMock: HttpTestingController;

  /** defaultInterceptor 会给相对地址补上 environment.host */
  const api = (path: string) => environment.host + path;

  const putTokenInStorage = (token: string) =>
    sessionStorage.setItem(
      'authentication',
      btoa(encodeURIComponent(JSON.stringify({ token, expires: 0, lastAccessTime: 0, details: {} }))),
    );

  beforeEach(() => {
    sessionStorage.clear();
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors(indexInterceptor)),
        provideHttpClientTesting(),
        provideRouter([]),
      ],
    });
    http = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('已登录时给自家接口带上 Bearer 令牌', () => {
    putTokenInStorage('T');
    http.get('/sec/users/page').subscribe();
    const req = httpMock.expectOne(api('/sec/users/page'));
    expect(req.request.headers.get('Authorization')).toBe('Bearer T');
    req.flush({});
  });

  it('登录请求不带令牌（它靠 Basic 凭据认证）', () => {
    putTokenInStorage('T');
    http.get('/sec/oauth2/login').subscribe();
    const req = httpMock.expectOne(api('/sec/oauth2/login'));
    expect(req.request.headers.has('Authorization')).toBe(false);
    req.flush({});
  });

  it('URL 含 /login 但不是登录端点的接口照样带令牌', () => {
    putTokenInStorage('T');
    http.get('/sec/users/login-history').subscribe();
    const req = httpMock.expectOne(api('/sec/users/login-history'));
    expect(req.request.headers.get('Authorization')).toBe('Bearer T');
    req.flush({});
  });

  it('第三方绝对地址不带令牌（不能把令牌交给别的域）', () => {
    putTokenInStorage('T');
    http.get('https://third.example.com/api').subscribe();
    const req = httpMock.expectOne('https://third.example.com/api');
    expect(req.request.headers.has('Authorization')).toBe(false);
    req.flush({});
  });

  it('调用方自带的 Authorization 被覆盖，而不是叠加成两个值', () => {
    putTokenInStorage('T');
    http.get('/sec/users/page', { headers: { Authorization: 'Basic abc' } }).subscribe();
    const req = httpMock.expectOne(api('/sec/users/page'));
    expect(req.request.headers.get('Authorization')).toBe('Bearer T');
    req.flush({});
  });

  it('没有令牌时请求根本不发，直接以 401 失败', () => {
    let status = 0;
    http.get('/sec/users/page').subscribe({ error: (e) => (status = e.status) });
    httpMock.expectNone(api('/sec/users/page'));
    expect(status).toBe(401);
  });
});

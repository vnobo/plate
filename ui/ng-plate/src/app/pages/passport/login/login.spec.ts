import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Component } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import dayjs from 'dayjs';
import { Login } from './login';

@Component({ template: '' })
class Dummy {}

/** 这四条各自守住一个真实踩过的坑，改动后跑 `ng test` 即可确认没有回退 */
describe('Login', () => {
  let httpMock: HttpTestingController;

  beforeEach(() => {
    document.querySelectorAll('tabler-toasts').forEach((el) => el.remove());
    localStorage.clear();
    sessionStorage.clear();
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([
          { path: 'dashboard', component: Dummy },
          { path: 'platform', component: Dummy },
        ]),
      ],
    });
    httpMock = TestBed.inject(HttpTestingController);
  });

  const submit = async (username: string, password: string, rememberMe = false) => {
    const fixture = TestBed.createComponent(Login);
    const cmp = fixture.componentInstance as any;
    cmp.loginModel.set({ username, password, rememberMe });
    await cmp.onSubmit();
    return { fixture, cmp };
  };

  /** 记住的凭据：savedAt 传 3 天前即已过期 */
  const rememberCredentials = (username = 'admin', password = 'secret123', savedAt = dayjs().valueOf()) =>
    localStorage.setItem('login-credentials', JSON.stringify({ username, password, savedAt }));

  /** 令牌：expires 是空闲时长（秒），这里给 30 分钟 */
  const putToken = (lastAccessTime = dayjs().unix(), expires = 1800) =>
    sessionStorage.setItem(
      'authentication',
      btoa(
        encodeURIComponent(
          JSON.stringify({ token: 't', expires, lastAccessTime, details: {} }),
        ),
      ),
    );

  it('中文凭据按 UTF-8 编码发送（btoa 直接吃中文会抛错，请求根本发不出去）', async () => {
    await submit('管理员账号', '密码123456');
    const req = httpMock.expectOne('/sec/oauth2/login');
    const raw = atob(req.request.headers.get('authorization')!.replace('Basic ', ''));
    const decoded = new TextDecoder().decode(Uint8Array.from(raw, (c) => c.charCodeAt(0)));
    expect(decoded).toBe('管理员账号:密码123456');
    req.flush({ token: 't', details: { nickname: '管理员' } });
  });

  it('勾选记住我把凭据存本地（含密码，2 天内自动登录用），不勾选则不留', async () => {
    await submit('admin', 'secret123', true);
    httpMock.expectOne('/sec/oauth2/login').flush({ token: 't', details: {} });
    const stored = localStorage.getItem('login-credentials') ?? '';
    expect(JSON.parse(stored).username).toBe('admin');
    expect(JSON.parse(stored).password).toBe('secret123');

    localStorage.clear();
    await submit('admin', 'secret123');
    httpMock.expectOne('/sec/oauth2/login').flush({ token: 't', details: {} });
    expect(localStorage.getItem('login-credentials')).toBeNull();
  });

  it('记住的凭据在 2 天内自动登录（进登录页直接带着账号密码发请求）', async () => {
    rememberCredentials();
    TestBed.createComponent(Login);
    const req = httpMock.expectOne('/sec/oauth2/login');
    const raw = atob(req.request.headers.get('authorization')!.replace('Basic ', ''));
    expect(raw).toBe('admin:secret123');
    req.flush({ token: 't', details: {} });
  });

  it('记住的凭据超过 2 天就失效（不再自动登录，也不留脏数据）', () => {
    rememberCredentials('admin', 'secret123', dayjs().valueOf() - 3 * 24 * 60 * 60 * 1000);
    TestBed.createComponent(Login);
    expect(() => httpMock.expectOne('/sec/oauth2/login')).toThrow();
    expect(localStorage.getItem('login-credentials')).toBeNull();
  });

  it('凭据被拒（401）就清掉本地那份（改过密码不该每次都白撞一次 401）', async () => {
    rememberCredentials();
    TestBed.createComponent(Login);
    httpMock
      .expectOne('/sec/oauth2/login')
      .flush({ path: '/oauth2/login' }, { status: 401, statusText: 'Unauthorized' });
    await new Promise((r) => setTimeout(r, 50));
    expect(localStorage.getItem('login-credentials')).toBeNull();
  });

  it('刷新后令牌没过期 → 直接回内页，不再停在登录页', async () => {
    putToken();
    const router = TestBed.inject(Router);
    TestBed.createComponent(Login);
    await new Promise((r) => setTimeout(r, 50));
    expect(router.url).toBe('/platform');
  });

  it('刷新后令牌已过期 → 留在登录页并清掉死令牌', async () => {
    putToken(dayjs().unix() - 3600);
    const router = TestBed.inject(Router);
    TestBed.createComponent(Login);
    await new Promise((r) => setTimeout(r, 50));
    expect(router.url).not.toBe('/platform');
    expect(sessionStorage.getItem('authentication')).toBeNull();
  });

  it('401 不重试（登录失败重发凭据只会放大锁定风险）', async () => {
    await submit('admin', 'secret123');
    httpMock
      .expectOne('/sec/oauth2/login')
      .flush('nope', { status: 401, statusText: 'Unauthorized' });
    expect(() => httpMock.expectOne('/sec/oauth2/login')).toThrow();
  });

  it('401 提示凭据错误，而不是笼统的"登录系统失败"', async () => {
    await submit('admin', 'secret123');
    httpMock
      .expectOne('/sec/oauth2/login')
      .flush({ path: '/oauth2/login' }, { status: 401, statusText: 'Unauthorized' });
    await new Promise((r) => setTimeout(r, 50));
    expect(document.querySelector('tabler-toasts')?.textContent).toContain('用户名或密码错误');
  });

  it('200 但没有令牌时不放行（否则会带着空令牌进内页再被踢回）', async () => {
    await submit('admin', 'secret123');
    httpMock.expectOne('/sec/oauth2/login').flush({ token: '', details: {} });
    await new Promise((r) => setTimeout(r, 50));
    expect(document.querySelector('tabler-toasts')?.textContent).toContain('缺少令牌');
  });

  it('组件销毁后自动退订（请求取消，回调不再执行）', async () => {
    const { fixture } = await submit('admin', 'secret123');
    const req = httpMock.expectOne('/sec/oauth2/login');
    fixture.destroy();
    expect(req.cancelled).toBe(true);
    await new Promise((r) => setTimeout(r, 30));
    expect(document.querySelector('tabler-toasts')).toBeNull();
  });
});

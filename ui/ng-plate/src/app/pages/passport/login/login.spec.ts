import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Component } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import dayjs from 'dayjs';
import { Login } from './login';

@Component({ template: '' })
class Dummy {}

/** 守住真实踩过的坑；改动后跑 `ng test` 即可确认没有回退 */
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

  /** 等一个宏任务，让 login.ts 里的异步链（如 refresh 订阅）跑完 */
  const tick = () => new Promise((r) => setTimeout(r, 25));

  const submit = async (username: string, password: string, rememberMe = false) => {
    const fixture = TestBed.createComponent(Login);
    const cmp = fixture.componentInstance as any;
    cmp.loginModel.set({ username, password, rememberMe });
    await cmp.onSubmit();
    await tick();
    return { fixture, cmp };
  };

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

  it('勾选记住我 → 登录请求带 rememberMe=true，不勾选则不带', async () => {
    await submit('admin', 'secret123', true);
    const req = httpMock.expectOne('/sec/oauth2/login?rememberMe=true');
    req.flush({ token: 't', details: {} });

    localStorage.clear();
    await submit('admin', 'secret123');
    // 不勾选：仍是普通登录地址，且前端不再往 localStorage 落任何凭据
    httpMock.expectOne('/sec/oauth2/login');
    expect(localStorage.getItem('login-credentials')).toBeNull();
  });

  it('记住我：进登录页用后端 cookie 换会话直接进内页（本地不存任何账号密码）', async () => {
    TestBed.createComponent(Login);
    await tick();
    const req = httpMock.expectOne('/sec/oauth2/refresh');
    req.flush({ token: 't', expires: 1800, lastAccessTime: dayjs().unix(), details: {} });
    await tick();
    expect(localStorage.getItem('login-credentials')).toBeNull();
    expect(TestBed.inject(Router).url).toBe('/platform');
  });

  it('记住我 cookie 失效（401）→ 留在登录页，不报错', async () => {
    TestBed.createComponent(Login);
    await tick();
    httpMock
      .expectOne('/sec/oauth2/refresh')
      .flush(null, { status: 401, statusText: 'Unauthorized' });
    await tick();
    expect(TestBed.inject(Router).url).not.toBe('/platform');
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
    expect(document.querySelector('tabler-toasts')?.textContent).toContain('Username or password is incorrect');
  });

  it('200 但没有令牌时不放行（否则会带着空令牌进内页再被踢回）', async () => {
    await submit('admin', 'secret123');
    httpMock.expectOne('/sec/oauth2/login').flush({ token: '', details: {} });
    await new Promise((r) => setTimeout(r, 50));
    expect(document.querySelector('tabler-toasts')?.textContent).toContain('missing token');
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

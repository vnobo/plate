import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Component } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { Login } from './login';

@Component({ template: '' })
class Dummy {}

/** 这四条各自守住一个真实踩过的坑，改动后跑 `ng test` 即可确认没有回退 */
describe('Login', () => {
  let httpMock: HttpTestingController;

  beforeEach(() => {
    document.querySelectorAll('tabler-toasts').forEach((el) => el.remove());
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([{ path: 'dashboard', component: Dummy }]),
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

  it('中文凭据按 UTF-8 编码发送（btoa 直接吃中文会抛错，请求根本发不出去）', async () => {
    await submit('管理员账号', '密码123456');
    const req = httpMock.expectOne('/sec/oauth2/login');
    const raw = atob(req.request.headers.get('authorization')!.replace('Basic ', ''));
    const decoded = new TextDecoder().decode(Uint8Array.from(raw, (c) => c.charCodeAt(0)));
    expect(decoded).toBe('管理员账号:密码123456');
    req.flush({ token: 't', details: { nickname: '管理员' } });
  });

  it('记住我只存用户名，密码绝不落盘', async () => {
    await submit('admin', 'secret123', true);
    httpMock.expectOne('/sec/oauth2/login').flush({ token: 't', details: {} });
    const stored = localStorage.getItem('login-username') ?? '';
    expect(stored).toContain('admin');
    expect(stored).not.toContain('secret');
    expect(localStorage.getItem('credentials')).toBeNull();
  });

  it('401 不重试（登录失败重发凭据只会放大锁定风险）', async () => {
    await submit('admin', 'secret123');
    httpMock
      .expectOne('/sec/oauth2/login')
      .flush('nope', { status: 401, statusText: 'Unauthorized' });
    expect(() => httpMock.expectOne('/sec/oauth2/login')).toThrow();
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

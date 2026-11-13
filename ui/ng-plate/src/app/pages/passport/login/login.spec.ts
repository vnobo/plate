import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Component } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import dayjs from 'dayjs';
import { Login } from './login';

@Component({ template: '' })
class Dummy {}

/** Guards a real pitfall we hit before; run `ng test` after changes to confirm no regression. */
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

  /** Waits one macrotask so the async login chain in login.ts finishes. */
  const tick = () => new Promise((r) => setTimeout(r, 25));

  const submit = async (username: string, password: string) => {
    const fixture = TestBed.createComponent(Login);
    const cmp = fixture.componentInstance as any;
    cmp.loginModel.set({ username, password });
    await cmp.onSubmit();
    await tick();
    return { fixture, cmp };
  };

  /** Token: expires is the idle lifetime in seconds; 30 minutes is given here. */
  const putToken = (lastAccessTime = dayjs().unix(), expires = 1800) =>
    sessionStorage.setItem(
      'authentication',
      btoa(
        encodeURIComponent(
          JSON.stringify({ token: 't', expires, lastAccessTime, details: {} }),
        ),
      ),
    );

  it('sends UTF-8 encoded credentials (btoa throws on raw CJK, so the request would never go out)', async () => {
    await submit('管理员账号', '密码123456');
    const req = httpMock.expectOne('/sec/oauth2/login');
    const raw = atob(req.request.headers.get('authorization')!.replace('Basic ', ''));
    const decoded = new TextDecoder().decode(Uint8Array.from(raw, (c) => c.charCodeAt(0)));
    expect(decoded).toBe('管理员账号:密码123456');
    req.flush({ token: 't', details: { nickname: '管理员' } });
  });

  it('navigates straight to the inner page when the token is still valid after a refresh', async () => {
    putToken();
    const router = TestBed.inject(Router);
    TestBed.createComponent(Login);
    await new Promise((r) => setTimeout(r, 50));
    expect(router.url).toBe('/platform');
  });

  it('stays on the login page and clears the stale token when it has expired', async () => {
    putToken(dayjs().unix() - 3600);
    const router = TestBed.inject(Router);
    TestBed.createComponent(Login);
    await new Promise((r) => setTimeout(r, 50));
    expect(router.url).not.toBe('/platform');
    expect(sessionStorage.getItem('authentication')).toBeNull();
  });

  it('does not retry on 401 (resending credentials would amplify account lockout risk)', async () => {
    await submit('admin', 'secret123');
    httpMock
      .expectOne('/sec/oauth2/login')
      .flush('nope', { status: 401, statusText: 'Unauthorized' });
    expect(() => httpMock.expectOne('/sec/oauth2/login')).toThrow();
  });

  it('shows a credential error on 401 instead of a generic login failure', async () => {
    await submit('admin', 'secret123');
    httpMock
      .expectOne('/sec/oauth2/login')
      .flush({ path: '/oauth2/login' }, { status: 401, statusText: 'Unauthorized' });
    await new Promise((r) => setTimeout(r, 50));
    expect(document.querySelector('tabler-toasts')?.textContent).toContain('Username or password is incorrect');
  });

  it('does not grant access on 200 without a token (an empty token would be bounced back from the inner page)', async () => {
    await submit('admin', 'secret123');
    httpMock.expectOne('/sec/oauth2/login').flush({ token: '', details: {} });
    await new Promise((r) => setTimeout(r, 50));
    expect(document.querySelector('tabler-toasts')?.textContent).toContain('missing token');
  });

  it('unsubscribes on destroy (request cancelled, callbacks no longer run)', async () => {
    const { fixture } = await submit('admin', 'secret123');
    const req = httpMock.expectOne('/sec/oauth2/login');
    fixture.destroy();
    expect(req.cancelled).toBe(true);
    await new Promise((r) => setTimeout(r, 30));
    expect(document.querySelector('tabler-toasts')).toBeNull();
  });
});

import { TestBed } from '@angular/core/testing';
import { provideRouter, UrlTree } from '@angular/router';

import { canActivateGuard } from './pages.guard';
import { TokenService } from './services/token.service';

describe('canActivateGuard', () => {
  const guardOn = (url: string) =>
    TestBed.runInInjectionContext(() =>
      canActivateGuard(null as never, { url } as never),
    );

  const store = (token: string) =>
    sessionStorage.setItem(
      'authentication',
      btoa(encodeURIComponent(JSON.stringify({ token, details: {} }))),
    );

  beforeEach(() => {
    sessionStorage.clear();
    TestBed.configureTestingModule({ providers: [provideRouter([])] });
  });

  it('未登录时拦下内页，并记下来路以便登录后跳回', () => {
    const result = guardOn('/platform/users');
    expect(result instanceof UrlTree).toBe(true);
    expect(TestBed.inject(TokenService).redirectUrl).toBe('/platform/users');
  });

  it('sessionStorage 里还有令牌时放行（刷新页面不该被登出）', () => {
    store('t');
    expect(guardOn('/platform/users')).toBe(true);
  });
});

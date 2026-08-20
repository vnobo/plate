import { HttpErrorResponse } from '@angular/common/http';
import { computed, inject, Service, signal } from '@angular/core';
import { toObservable } from '@angular/core/rxjs-interop';
import { Authentication } from '@plate/types';
import { SessionStorage } from '@app/core';
import dayjs from 'dayjs';

@Service()
export class TokenService {
  readonly loginUrl = '/passport/login';
  redirectUrl = '/dashboard';

  private readonly authenticationKey = 'authentication';
  private readonly _storage = inject(SessionStorage);

  private readonly isLoggedIn = signal(false);
  private readonly authentication = signal<Authentication | null>(null);

  readonly isLoggedIn$ = toObservable(this.isLoggedIn);
  readonly authentication$ = toObservable(this.authentication);

  /** 当前登录用户名（昵称优先，否则姓名，否则账号） */
  readonly name = computed(() => {
    const details = this.authentication()?.details;
    if (!details) return '未登录';
    return details.nickname || details.name || details.code || '用户';
  });

  /** 当前用户首字母，用于头像占位 */
  readonly initial = computed(() => this.name().charAt(0).toUpperCase());

  /** 当前用户角色（拼接 authority） */
  readonly role = computed(() => {
    const authorities = this.authentication()?.details?.authorities;
    if (!authorities?.length) return '';
    return authorities.map((a) => (typeof a === 'string' ? a : a.authority)).join(' / ');
  });

  hasRole(role: string): boolean {
    const auth = this.authentication();
    if (!auth?.details?.authorities) {
      return false;
    }
    return auth.details.authorities.some((authority) =>
      typeof authority === 'string' ? authority === role : authority.authority === role,
    );
  }

  hasAnyRole(roles: string[]): boolean {
    if (!roles?.length) return false;

    const auth = this.authentication();
    if (!auth?.details?.authorities) return false;

    return roles.some((role) =>
      auth.details!.authorities.some((authority) =>
        typeof authority === 'string' ? authority === role : authority.authority === role,
      ),
    );
  }

  hasAllRoles(roles: string[]): boolean {
    if (!roles?.length) return true;

    const auth = this.authentication();
    if (!auth?.details?.authorities) return false;

    return roles.every((role) =>
      auth.details!.authorities.some((authority) =>
        typeof authority === 'string' ? authority === role : authority.authority === role,
      ),
    );
  }

  getUserRoles(): string[] {
    const auth = this.authentication();
    if (!auth?.details?.authorities) return [];

    return auth.details.authorities.map((authority) =>
      typeof authority === 'string' ? authority : authority.authority,
    );
  }

  authenticationToken(): Authentication | null {
    if (this.isLoggedIn()) {
      return this.authentication();
    }
    const authentication = this.authenticationLoadStorage();
    if (authentication) {
      authentication.lastAccessTime = dayjs().unix();
      this.login(authentication);
      return authentication;
    }
    return null;
  }

  isLogged(): boolean {
    return this.isLoggedIn();
  }

  authToken(): string {
    if (this.isLoggedIn()) {
      return this.authentication()!.token;
    }
    const authentication = this.authenticationLoadStorage();
    if (authentication) {
      authentication.lastAccessTime = dayjs().unix();
      this.login(authentication);
      return authentication.token;
    }
    throw new HttpErrorResponse({
      error: 'Authentication is invalid, please log in again.',
      status: 401,
    });
  }

  login(authentication: Authentication): void {
    this.isLoggedIn.set(true);
    this.authentication.set(authentication);
    const jsonStr = JSON.stringify(authentication);
    const enstr = btoa(encodeURIComponent(jsonStr));
    this._storage.setItem(this.authenticationKey, enstr);
  }

  logout(): void {
    this.isLoggedIn.set(false);
    this.authentication.set(null);
    this._storage.removeItem(this.authenticationKey);
  }

  private authenticationLoadStorage(): Authentication | null {
    const authenticationJsonStr = this._storage.getItem(this.authenticationKey);
    if (authenticationJsonStr) {
      try {
        const authentication: Authentication = JSON.parse(
          decodeURIComponent(atob(authenticationJsonStr)),
        );
        if (authentication && authentication.token) {
          return authentication;
        }
      } catch {
        this.logout();
      }
    }
    return null;
  }
}

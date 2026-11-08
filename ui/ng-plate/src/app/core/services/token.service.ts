import { HttpErrorResponse } from '@angular/common/http';
import { computed, inject, Service, signal } from '@angular/core';
import { toObservable } from '@angular/core/rxjs-interop';
import { Authentication } from '@plate/types';
import { SessionStorage } from '../storage/session-storage';
import dayjs from 'dayjs';

@Service()
export class TokenService {
  readonly loginUrl = '/passport/login';
  redirectUrl = '/platform';

  private readonly authenticationKey = 'authentication';
  private readonly _storage = inject(SessionStorage);

  private readonly isLoggedIn = signal(false);
  private readonly authentication = signal<Authentication | null>(null);

  readonly isLoggedIn$ = toObservable(this.isLoggedIn);
  readonly authentication$ = toObservable(this.authentication);

  /** 当前会话的只读快照；用户展示信息（姓名/昵称/头像）请走 UserService */
  readonly current = computed(() => this.authentication());

  hasRole(role: string): boolean {
    const auth = this.authentication();
    if (!auth?.details?.authorities) {
      return false;
    }
    return auth.details.authorities.some((authority) =>
      typeof authority === 'string' ? authority === role : authority.authority === role,
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



  /**
   * 存储里的令牌快照，不刷新 lastAccessTime —— 判过期必须用它：
   * authenticationToken() 会顺手把 lastAccessTime 改成现在，拿那个值判过期永远不过期。
   */
  peekAuthentication(): Authentication | null {
    return this.authenticationLoadStorage();
  }

  /**
   * 令牌是否过期。
   * 后端 AuthenticationToken.expires 是「会话最大空闲时长（秒）」而不是绝对到期时间戳，
   * 所以要拿 lastAccessTime + expires 跟现在比；expires 缺失/为 0 时不判过期，交给后端 401 兜底。
   */
  isExpired(authentication: Authentication | null): boolean {
    if (!authentication?.expires) {
      return false;
    }
    const deadline = (authentication.lastAccessTime ?? 0) + authentication.expires;
    return dayjs().unix() > deadline;
  }

  private authenticationLoadStorage(): Authentication | null {
    return this.parse(this._storage.getItem(this.authenticationKey));
  }

  private parse(authenticationJsonStr: string | null): Authentication | null {
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

import { HttpErrorResponse } from '@angular/common/http';
import { afterNextRender, inject, Injectable, signal } from '@angular/core';
import { Authentication } from '@plate/types';
import { SessionStorage } from '@app/core';
import dayjs from 'dayjs';
import { toObservable } from '@angular/core/rxjs-interop';

@Injectable({ providedIn: 'root' })
export class TokenService {
  public readonly loginUrl = '/passport/login';
  private readonly authenticationKey = 'authentication';
  private readonly _storage = inject(SessionStorage);

  private isLoggedIn = signal(false);
  private authentication = signal({} as Authentication);

  public redirectUrl = '/home';

  public isLoggedIn$ = toObservable(this.isLoggedIn);
  public authentication$ = toObservable(this.authentication);

  constructor() {
    afterNextRender(() => {});
  }

  /**
   * Checks if the current user has a specific role/authority
   * @param role The role/authority to check for
   * @returns boolean indicating whether the user has the specified role
   */
  hasRole(role: string): boolean {
    // Check if authentication exists and has details
    const auth = this.authentication();
    if (!auth || !auth.details.authorities) {
      return false;
    }

    // Check if the role exists in the user's authorities
    // Authorities are stored as objects with an 'authority' property
    return auth.details.authorities.some(authority =>
      typeof authority === 'string'
        ? authority === role
        : authority.authority === role
    );
  }

  /**
   * Checks if the current user has any of the specified roles/authorities
   * @param roles Array of roles/authorities to check for
   * @returns boolean indicating whether the user has at least one of the specified roles
   */
  hasAnyRole(roles: string[]): boolean {
    if (!roles || roles.length === 0) {
      return false;
    }
    
    const auth = this.authentication();
    if (!auth || !auth.details.authorities) {
      return false;
    }

    // Check if any of the provided roles exist in the user's authorities
    return roles.some(role =>
      auth.details.authorities.some(authority =>
        typeof authority === 'string'
          ? authority === role
          : authority.authority === role
      )
    );
  }

  /**
   * Checks if the current user has all of the specified roles/authorities
   * @param roles Array of roles/authorities to check for
   * @returns boolean indicating whether the user has all of the specified roles
   */
  hasAllRoles(roles: string[]): boolean {
    if (!roles || roles.length === 0) {
      return true; // If no roles specified, condition is satisfied
    }
    
    const auth = this.authentication();
    if (!auth || !auth.details.authorities) {
      return false;
    }

    // Check if all of the provided roles exist in the user's authorities
    return roles.every(role =>
      auth.details.authorities.some(authority =>
        typeof authority === 'string'
          ? authority === role
          : authority.authority === role
      )
    );
  }

  /**
   * Gets all roles/authorities for the current user
   * @returns Array of role strings
   */
  getUserRoles(): string[] {
    const auth = this.authentication();
    if (!auth || !auth.details.authorities) {
      return [];
    }

    // Extract role strings from authority objects or return as-is if already strings
    return auth.details.authorities.map(authority =>
      typeof authority === 'string' ? authority : authority.authority
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
      return this.authentication().token;
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
    var jsonStr = JSON.stringify(authentication);
    var enstr = btoa(encodeURIComponent(jsonStr));
    this._storage.setItem(this.authenticationKey, enstr);
  }

  logout(): void {
    this.isLoggedIn.set(false);
    this.authentication.set({} as Authentication);
    this._storage.removeItem(this.authenticationKey);
  }

  private authenticationLoadStorage(): Authentication | null {
    const authenticationJsonStr = this._storage.getItem(this.authenticationKey);
    if (authenticationJsonStr) {
      const authentication: Authentication = JSON.parse(
        decodeURIComponent(atob(authenticationJsonStr)),
      );
      const lastAccessTime = dayjs.unix(authentication.lastAccessTime);
      const diffSec = dayjs().diff(lastAccessTime, 'second');
      if (diffSec < authentication.expires) {
        return authentication;
      }
      this._storage.removeItem(this.authenticationKey);
    }
    return null;
  }
}

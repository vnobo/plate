import {afterNextRender, Inject, inject, Injectable, InjectionToken} from '@angular/core';
import {Subject} from "rxjs";
import {HttpErrorResponse} from "@angular/common/http";
import {CanActivateChildFn, CanActivateFn, CanMatchFn, Router} from "@angular/router";

export const SESSION_STORAGE = new InjectionToken<Storage>('Session Storage', {
  providedIn: 'root',
  factory: () => sessionStorage
});

export const BROWSER_STORAGE = new InjectionToken<Storage>('Browser Storage', {
  providedIn: 'root',
  factory: () => localStorage
});

@Injectable({providedIn: 'root'})
export class BrowserStorageService {

  private storage: Storage = localStorage;

  constructor(@Inject(BROWSER_STORAGE) private _storage: Storage) {
    afterNextRender(() => {
      this.storage = this._storage;
    });
  }

  get(key: string) {
    return this.storage.getItem(key);
  }

  delete(key: string) {
    return this.storage.removeItem(key);
  }

  set(key: string, value: string) {
    this.storage.setItem(key, value);
  }
}

export const authGuard: CanMatchFn | CanActivateFn | CanActivateChildFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  if (auth.isLoggedIn) {
    return true;
  }

  return router.parseUrl(auth.loginUrl);
}

@Injectable({providedIn: 'root'})
export class AuthService {
  isLoggedIn = false;
  // store the URL so we can redirect after logging in
  loginUrl = '/auth/login';
  private sessionKey = 'x-auth-token';
  private authenticatedSource = new Subject<boolean>();
  authenticated$ = this.authenticatedSource.asObservable();

  constructor(private storage: BrowserStorageService) {
    this.autoLogin();
  }

  autoLogin() {
    const authToken = this.loadSessionByStorage();
    if (authToken != null) {
      this.isLoggedIn = true;
      this.authenticatedSource.next(true);
    }
  }

  authToken(): string {
    const authToken = this.loadSessionByStorage();
    if (authToken == null) {
      throw new HttpErrorResponse({error: "Authenticate is incorrectness,please login again.", status: 401});
    }
    return authToken;
  }

  loadSessionByStorage(): string | null {
    const authToken = this.storage.get(this.sessionKey);
    if (authToken != null) {
      return authToken;
    }
    return null;
  }

  login(authentication: string) {
    this.isLoggedIn = true;
    this.authenticatedSource.next(true);
    this.storage.set(this.sessionKey, authentication);
  }

  logout(): void {
    this.isLoggedIn = false;
    this.authenticatedSource.next(false);
    this.storage.delete(this.sessionKey);
  }

}

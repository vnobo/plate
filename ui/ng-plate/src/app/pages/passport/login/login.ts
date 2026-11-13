import { HttpClient, HttpErrorResponse, HttpHeaders } from '@angular/common/http';
import { Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormField, form, maxLength, minLength, required, submit } from '@angular/forms/signals';
import { Router } from '@angular/router';
import { TokenService } from '@app/core';
import { MessageService } from '@app/plugins';
import { Authentication } from '@plate/types';
import { tap } from 'rxjs';

@Component({
  selector: 'app-login',
  imports: [FormField],
  templateUrl: './login.html',
  styleUrl: './login.scss',
})
export class Login {
  private readonly _tokenSer = inject(TokenService);
  private readonly _message = inject(MessageService);
  private readonly _http = inject(HttpClient);
  private readonly _router = inject(Router);
  private readonly _destroyRef = inject(DestroyRef);

  passwordFieldTextType = signal(false);
  isSubmitting = signal(false);

  protected readonly loginModel = signal({
    username: '',
    password: '',
    rememberMe: false,
  });

  protected readonly loginForm = form(this.loginModel, (p) => {
    required(p.username, { message: 'Please enter username' });
    minLength(p.username, 5, { message: 'Username must be at least 5 characters' });
    maxLength(p.username, 64, { message: 'Username cannot exceed 64 characters' });
    required(p.password, { message: 'Please enter password' });
    minLength(p.password, 6, { message: 'Password must be at least 6 characters' });
    maxLength(p.password, 32, { message: 'Password cannot exceed 32 characters' });
  });

  constructor() {
    // After a refresh, a token may still sit in sessionStorage: skip to the inner page if valid, otherwise stay on the login page.
    // SSR has no storage, so authenticationToken() returns null and we will not jump by mistake.
    // We judge expiry from the peeked copy: authenticationToken() refreshes lastAccessTime, which would make it look never-expired.
    const authentication = this._tokenSer.peekAuthentication();
    if (authentication) {
      if (!this._tokenSer.isExpired(authentication)) {
        this._tokenSer.login(authentication);
        void this._router.navigate([this._tokenSer.redirectUrl]);
        return;
      }
      // A dead token would only be rejected by 401 on the next request, so clear it now.
      this._tokenSer.logout();
    }
  }

  /** First error message for a field; only shown after the user touched it, to avoid a page full of red on entry. */
  protected errorOf(field: 'username' | 'password'): string {
    const f = this.loginForm[field]();
    if (!f.invalid() || !(f.dirty() || f.touched())) return '';
    const [first] = f.errors() as Array<{ message?: unknown }>;
    return typeof first?.message === 'string' ? first.message : 'Invalid input';
  }

  async onSubmit() {
    if (this.isSubmitting()) {
      return;
    }

    await submit(this.loginForm, {
      action: async () => {
        await this.formProcessLogin();
      },
    });
  }

  showPassword() {
    this.passwordFieldTextType.update((v) => !v);
  }

  private async formProcessLogin() {
    this.isSubmitting.set(true);
    try {
      const credentials = this.loginModel();
      const headers = new HttpHeaders({
        authorization: this.basicAuth(credentials.username, credentials.password),
      });
      this.login(headers, credentials.rememberMe).subscribe({
        next: () => {
          // A 200 without a token is not success: entering the inner page with an empty token would be bounced back by 401 on the next request.
          if (!this._tokenSer.authenticationToken()?.token) {
            this._message.error('Login response is missing token, please contact the administrator', {
              autohide: true,
              animation: true,
              delay: 5000,
            });
            this.isSubmitting.set(false);
            return;
          }
          this.handleLoginSuccess(credentials.username);
        },
        error: (error) => {
          this.handleLoginError(error);
          this.isSubmitting.set(false);
        },
        complete: () => this.isSubmitting.set(false),
      });
    } catch (error) {
      console.error('[login] unexpected error', error);
      this._message.error('Login failed, please try again later', {
        autohide: false,
        animation: false,
        delay: 1000,
      });
      this.isSubmitting.set(false);
    }
  }

  /**
   * btoa only accepts code points <= 255; a CJK credential would throw InvalidCharacterError,
   * so we take the UTF-8 bytes first and then convert to a binary string.
   */
  private basicAuth(username: string, password: string): string {
    const bytes = new TextEncoder().encode(`${username}:${password}`);
    let binary = '';
    for (const byte of bytes) {
      binary += String.fromCharCode(byte);
    }
    return 'Basic ' + btoa(binary);
  }

  private login(headers: HttpHeaders, rememberMe: boolean) {
    const url = rememberMe ? '/sec/oauth2/login?rememberMe=true' : '/sec/oauth2/login';
    return this._http.get<Authentication>(url, { headers }).pipe(
      tap((authentication: Authentication) => this._tokenSer.login(authentication)),
      // Do not retry the login request: retrying on 401 just resends the credentials and increases lockout risk.
      // Unsubscribe on destroy to avoid callbacks acting on a destroyed component.
      takeUntilDestroyed(this._destroyRef),
    );
  }

  private handleLoginSuccess(username: string) {
    // Fall back to the just-entered account when the nickname is missing, to avoid a "Welcome, Not signed in!" greeting.
    const name = this._tokenSer.authenticationToken()?.details?.nickname || username;
    this._message.success('Login successful, welcome ' + name + '!', {
      autohide: true,
      delay: 5000,
      animation: true,
    });
    // redirectUrl is recorded as an absolute path by pages.guard, so no relativeTo is needed.
    void this._router.navigate([this._tokenSer.redirectUrl]);
  }

  private handleLoginError(error: unknown) {
    console.error('[login] failed', error);
    // 401 is unwrapped into a response body by handleErrorInterceptor (no longer an HttpErrorResponse),
    // so reaching here from the login API can only mean rejected credentials; status 0 means unreachable / timed out.
    const status = error instanceof HttpErrorResponse ? error.status : 401;
    const errorMessage =
      status === 0
        ? 'Cannot connect to the server, please check your network and try again'
        : status === 401 || status === 403
          ? 'Username or password is incorrect, please try again'
          : status === 429
            ? 'Too many attempts, please try again later'
            : status >= 500
              ? 'Service temporarily unavailable, please try again later'
              : 'Login failed, please check your username and password';
    this._message.error(errorMessage, {
      autohide: true,
      animation: true,
      delay: 5000,
    });
  }
}

import { HttpClient, HttpErrorResponse, HttpHeaders } from '@angular/common/http';
import { Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormField, form, maxLength, minLength, required, submit } from '@angular/forms/signals';
import { Router } from '@angular/router';
import { TokenService } from '@app/core';
import { LocaleSwitcher } from '@app/layout/locale-switcher';
import { MessageService } from '@app/plugins';
import { Authentication } from '@plate/types';
import { tap } from 'rxjs';

@Component({
  selector: 'app-login',
  imports: [FormField, LocaleSwitcher],
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
  });

  protected readonly loginForm = form(this.loginModel, (p) => {
    required(p.username, { message: $localize`:@@login.usernameRequired:请输入用户名` });
    minLength(p.username, 5, { message: $localize`:@@login.usernameMinLength:用户名至少 5 个字符` });
    maxLength(p.username, 64, { message: $localize`:@@login.usernameMaxLength:用户名不能超过 64 个字符` });
    required(p.password, { message: $localize`:@@login.passwordRequired:请输入密码` });
    minLength(p.password, 6, { message: $localize`:@@login.passwordMinLength:密码至少 6 个字符` });
    maxLength(p.password, 32, { message: $localize`:@@login.passwordMaxLength:密码不能超过 32 个字符` });
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
    return typeof first?.message === 'string' ? first.message : $localize`:@@login.invalidInput:输入无效`;
  }

  /** Accessible label for the password visibility toggle; kept in the catalog via $localize. */
  protected passwordToggleLabel(): string {
    return this.passwordFieldTextType()
      ? $localize`:@@login.hidePassword:隐藏密码`
      : $localize`:@@login.showPassword:显示密码`;
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
      this.login(headers).subscribe({
        next: () => {
          // A 200 without a token is not success: entering the inner page with an empty token would be bounced back by 401 on the next request.
          if (!this._tokenSer.authenticationToken()?.token) {
            this._message.error($localize`:@@login.missingToken:登录响应缺少令牌，请联系管理员`, {
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
      this._message.error($localize`:@@login.unexpectedError:登录失败，请稍后重试`, {
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

  private login(headers: HttpHeaders) {
    return this._http.get<Authentication>('/sec/oauth2/login', { headers }).pipe(
      tap((authentication: Authentication) => this._tokenSer.login(authentication)),
      // Do not retry the login request: retrying on 401 just resends the credentials and increases lockout risk.
      // Unsubscribe on destroy to avoid callbacks acting on a destroyed component.
      takeUntilDestroyed(this._destroyRef),
    );
  }

  private handleLoginSuccess(username: string) {
    // Fall back to the just-entered account when the nickname is missing, to avoid a "Welcome, Not signed in!" greeting.
    const name = this._tokenSer.authenticationToken()?.details?.nickname || username;
    this._message.success($localize`:@@login.welcome:登录成功，欢迎 ${name}！`, {
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
        ? $localize`:@@login.networkError:无法连接服务器，请检查网络后重试`
        : status === 401 || status === 403
          ? $localize`:@@login.badCredentials:用户名或密码错误，请重试`
          : status === 429
            ? $localize`:@@login.tooManyAttempts:尝试次数过多，请稍后再试`
            : status >= 500
              ? $localize`:@@login.serviceUnavailable:服务暂时不可用，请稍后再试`
              : $localize`:@@login.loginFailed:登录失败，请检查用户名和密码`;
    this._message.error(errorMessage, {
      autohide: true,
      animation: true,
      delay: 5000,
    });
  }
}

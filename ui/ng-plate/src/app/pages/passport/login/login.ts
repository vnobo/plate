import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormField, form, maxLength, minLength, required, submit } from '@angular/forms/signals';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { BrowserStorage, TokenService } from '@app/core';
import { MessageService } from '@app/plugins';
import { Authentication } from '@plate/types';
import { retry, tap } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

@Component({
  selector: 'app-login',
  imports: [FormField, RouterLink],
  templateUrl: './login.html',
  styleUrl: './login.scss',
})
export class Login {
  private readonly _tokenSer = inject(TokenService);
  private readonly _message = inject(MessageService);
  private readonly _http = inject(HttpClient);
  private readonly _router = inject(Router);
  private readonly _route = inject(ActivatedRoute);
  private readonly _storage = inject(BrowserStorage);

  passwordFieldTextType = signal(false);
  isSubmitting = signal(false);

  protected readonly loginModel = signal({
    username: '',
    password: '',
    rememberMe: false,
  });

  protected readonly loginForm = form(this.loginModel, (p) => {
    required(p.username, { message: '请输入用户名' });
    minLength(p.username, 5, { message: '用户名至少5个字符' });
    maxLength(p.username, 64, { message: '用户名不能超过64个字符' });
    required(p.password, { message: '请输入密码' });
    minLength(p.password, 6, { message: '密码至少6个字符' });
    maxLength(p.password, 32, { message: '密码不能超过32个字符' });
  });

  constructor() {
    this.processLogin();
    this.loadRememberedCredentials();
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
        authorization: 'Basic ' + btoa(credentials.username + ':' + credentials.password),
      });
      this.login(headers).subscribe({
        next: (authentication: Authentication) => {
          if (credentials.rememberMe) {
            this.storeCredentials(credentials);
          } else {
            this.clearStoredCredentials();
          }
          this.handleLoginSuccess(authentication);
        },
        error: (error) => {
          this.handleLoginError(error);
          this.isSubmitting.set(false);
        },
        complete: () => this.isSubmitting.set(false),
      });
    } catch (error) {
      this._message.error('登录失败，请稍后再试! 错误: ' + (error || '未知错误'), {
        autohide: false,
        animation: false,
        delay: 1000,
      });
      this.isSubmitting.set(false);
    }
  }

  private processLogin() {
    const headers = new HttpHeaders({ 'x-requested-token': 'none-token-auto-login' });
    this.login(headers)
      .pipe(takeUntilDestroyed())
      .subscribe({
        next: (authentication: Authentication) => this.handleLoginSuccess(authentication),
        error: (error) => console.error(error.message),
      });
  }

  private loadRememberedCredentials() {
    const storedCredentials = this._storage.getItem('credentials');
    if (storedCredentials) {
      try {
        const credentials = JSON.parse(atob(storedCredentials));
        this.loginModel.update((m) => ({
          ...m,
          username: credentials.username,
          password: credentials.password,
          rememberMe: false,
        }));
      } catch {
        this.clearStoredCredentials();
      }
    }
  }

  private storeCredentials(credentials: { username: string; password: string }) {
    const credentialsToStore = {
      username: credentials.username,
      password: credentials.password,
      remember: true,
    };
    const encodedCredentials = btoa(JSON.stringify(credentialsToStore));
    this._storage.setItem('credentials', encodedCredentials);
  }

  private clearStoredCredentials() {
    this._storage.removeItem('credentials');
  }

  private login(headers: HttpHeaders) {
    return this._http.get<Authentication>('/sec/oauth2/login', { headers }).pipe(
      tap((authentication: Authentication) => this._tokenSer.login(authentication)),
      retry(3),
    );
  }

  private handleLoginSuccess(authentication: Authentication) {
    this._message.success('登录成功, 欢迎 ' + (authentication.details?.nickname as string) + '!', {
      autohide: true,
      delay: 5000,
      animation: true,
    });
    void this._router.navigate([this._tokenSer.redirectUrl], { relativeTo: this._route });
  }

  private handleLoginError(error: unknown) {
    const errorRecord = error as Record<string, string>;
    const errorMessage =
      errorRecord?.['errors'] ||
      (error as Error)?.message ||
      '登录系统失败，请检查您的用户名和密码';
    this._message.error(errorMessage, {
      autohide: true,
      animation: true,
      delay: 5000,
    });
  }
}

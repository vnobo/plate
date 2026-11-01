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
    required(p.username, { message: '请输入用户名' });
    minLength(p.username, 5, { message: '用户名至少5个字符' });
    maxLength(p.username, 64, { message: '用户名不能超过64个字符' });
    required(p.password, { message: '请输入密码' });
    minLength(p.password, 6, { message: '密码至少6个字符' });
    maxLength(p.password, 32, { message: '密码不能超过32个字符' });
  });

  constructor() {
    // 刷新后 sessionStorage 里还有令牌：没过期直接回内页，过期就留在登录页重新登录。
    // SSR 没有存储，authenticationToken() 返回 null，不会误跳。
    // 用 peek 那份判过期：authenticationToken() 会刷新 lastAccessTime，判出来永远不过期
    const authentication = this._tokenSer.peekAuthentication();
    if (authentication) {
      if (!this._tokenSer.isExpired(authentication)) {
        this._tokenSer.login(authentication);
        void this._router.navigate([this._tokenSer.redirectUrl]);
        return;
      }
      // 死令牌留着只会被下一个请求 401 再踢一次，先清掉
      this._tokenSer.logout();
    }
    this.autoLoginWithRemembered();
  }

  /** 字段的首条错误文案；用户动过该字段后才提示，避免一进页面就一片红 */
  protected errorOf(field: 'username' | 'password'): string {
    const f = this.loginForm[field]();
    if (!f.invalid() || !(f.dirty() || f.touched())) return '';
    const [first] = f.errors() as Array<{ message?: unknown }>;
    return typeof first?.message === 'string' ? first.message : '输入有误';
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
          // 200 但没有令牌不算成功：带着空令牌进内页，下一个请求就会被 401 踢回登录页
          if (!this._tokenSer.authenticationToken()?.token) {
            this._message.error('登录响应缺少令牌，请联系管理员', {
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
      this._message.error('登录失败，请稍后再试', {
        autohide: false,
        animation: false,
        delay: 1000,
      });
      this.isSubmitting.set(false);
    }
  }

  /**
   * btoa 只接受码点 <= 255 的字符，中文凭据会抛 InvalidCharacterError，
   * 所以先按 UTF-8 取字节再转 binary string。
   */
  private basicAuth(username: string, password: string): string {
    const bytes = new TextEncoder().encode(`${username}:${password}`);
    let binary = '';
    for (const byte of bytes) {
      binary += String.fromCharCode(byte);
    }
    return 'Basic ' + btoa(binary);
  }

  /**
   * 凭后端下发的 HttpOnly remember-me cookie 换一个新会话。
   * 前端不存密码、也不存 token —— 密码换来的只是一个 2 天有效、服务端可吊销的随机串。
   * 没有 cookie 或已被吊销 → 401，留在登录页让用户手输。
   */
  private autoLoginWithRemembered() {
    this._http.get<Authentication>('/sec/oauth2/refresh').subscribe({
      next: (authentication) => {
        // 有响应但没令牌不放行：带着空令牌进内页，下一个请求就会被 401 踢回登录页
        if (!authentication?.token) return;
        this._tokenSer.login(authentication);
        void this._router.navigate([this._tokenSer.redirectUrl]);
      },
      // 401 = 没记住过/已过期/已退出，正常留在本页，不用提示
      error: () => {},
    });
  }

  private login(headers: HttpHeaders, rememberMe: boolean) {
    const url = rememberMe ? '/sec/oauth2/login?rememberMe=true' : '/sec/oauth2/login';
    return this._http.get<Authentication>(url, { headers }).pipe(
      tap((authentication: Authentication) => this._tokenSer.login(authentication)),
      // 登录请求不重试：401 重试等于把凭据再发一遍，只会放大锁定风险
      // 组件销毁即退订，避免回调作用在已销毁的组件上
      takeUntilDestroyed(this._destroyRef),
    );
  }

  private handleLoginSuccess(username: string) {
    // 昵称缺失时退回刚输入的账号，避免出现「欢迎 未登录!」
    const name = this._tokenSer.authenticationToken()?.details?.nickname || username;
    this._message.success('登录成功, 欢迎 ' + name + '!', {
      autohide: true,
      delay: 5000,
      animation: true,
    });
    // redirectUrl 由 pages.guard 记为绝对路径，无需 relativeTo
    void this._router.navigate([this._tokenSer.redirectUrl]);
  }

  private handleLoginError(error: unknown) {
    console.error('[login] failed', error);
    // 401 会被 handleErrorInterceptor 剥成响应体（不再是 HttpErrorResponse），
    // 登录接口走到这里只可能是凭据被拒；status 0 则是连不上/超时
    const status = error instanceof HttpErrorResponse ? error.status : 401;
    const errorMessage =
      status === 0
        ? '无法连接服务器，请检查网络后重试'
        : status === 401 || status === 403
          ? '用户名或密码错误，请重试'
          : status === 429
          ? '尝试过于频繁，请稍后再试'
          : status >= 500
            ? '服务暂时不可用，请稍后再试'
            : '登录系统失败，请检查您的用户名和密码';
    this._message.error(errorMessage, {
      autohide: true,
      animation: true,
      delay: 5000,
    });
  }
}

import { CommonModule } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { afterNextRender, Component, inject, OnDestroy, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { TokenService } from '@app/core';
import { MessageService } from '@app/plugins';
import { Authentication, Credentials } from '@plate/types';
import {
  debounceTime,
  distinctUntilChanged,
  Observable,
  retry,
  Subject,
  takeUntil,
  tap,
} from 'rxjs';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule, RouterModule, CommonModule],
  templateUrl: './login.html',
  styleUrl: './login.scss',
})
export class Login implements OnDestroy {
  private readonly _tokenSer = inject(TokenService);
  private readonly _message = inject(MessageService);
  private readonly _http = inject(HttpClient);
  private readonly _router = inject(Router);
  private readonly _route = inject(ActivatedRoute);

  private submitSubject$ = new Subject<void>();
  private destroy$ = new Subject<void>();

  passwordFieldTextType = signal(false);
  isSubmitting = signal(false);

  loginForm = new FormGroup({
    username: new FormControl('', {
      validators: [Validators.required, Validators.minLength(5), Validators.maxLength(64)],
      nonNullable: true,
    }),
    password: new FormControl('', {
      validators: [Validators.required, Validators.minLength(6), Validators.maxLength(32)],
      nonNullable: true,
    }),
  });

  constructor() {
    afterNextRender(() => {
      this.submitSubject$
        .pipe(debounceTime(300), takeUntil(this.destroy$))
        .subscribe(() => this.processLogin());
      var authentication = this._tokenSer.authenticationToken();
      if (authentication != null) {
        // 如果 token 未过期，直接跳转到系统主界面
        if (!this.isTokenExpired(authentication)) {
          this.handleLoginSuccess(authentication);
        }
      }
    });
  }

  onSubmit() {
    if (this.loginForm.invalid || this.isSubmitting()) {
      return;
    }
    if (
      this.loginForm.get('username')?.hasError('required') ||
      this.loginForm.get('password')?.hasError('required')
    ) {
      this.isSubmitting.set(false);
      return;
    }

    if (
      this.loginForm.get('username')?.hasError('minlength') ||
      this.loginForm.get('username')?.hasError('maxlength')
    ) {
      this.isSubmitting.set(false);
      return;
    }

    if (
      this.loginForm.get('password')?.hasError('minlength') ||
      this.loginForm.get('password')?.hasError('maxlength')
    ) {
      this.isSubmitting.set(false);
      return;
    }

    this.submitSubject$.next();
  }

  private processLogin() {
    this.isSubmitting.set(true);
    try {
      const credentials = this.loginForm.getRawValue();
      this.login(credentials).subscribe({
        next: authentication => this.handleLoginSuccess(authentication),
        error: error => {
          this.handleLoginError(error);
          this.isSubmitting.set(false);
        },
        complete: () => this.isSubmitting.set(false),
      });
    } catch (error) {
      this._message.error('登录失败，请稍后再试! 错误: ' + (error || '未知错误'), {
        autohide: false,
        animation: false,
        delay: 100000,
      });
      this.isSubmitting.set(false);
    }
  }

  showPassword() {
    this.passwordFieldTextType.set(!this.passwordFieldTextType());
  }

  private login(credentials: Credentials): Observable<Authentication> {
    const headers = new HttpHeaders({
      authorization: 'Basic ' + btoa(credentials.username + ':' + credentials.password),
    });
    return this._http.get<Authentication>('/sec/v1/oauth2/login', { headers: headers }).pipe(
      debounceTime(300),
      distinctUntilChanged(),
      retry({ count: 3, delay: 1000 }),
      takeUntil(this.destroy$),
      tap(authentication => this._tokenSer.login(authentication)),
    );
  }

  private handleLoginSuccess(authentication: Authentication) {
    this._message.success(
      '登录成功, 欢迎 ' + (authentication.details?.['nickname'] as string) + '!',
      {
        autohide: true,
        delay: 5000,
        animation: true,
      },
    );
    this._router.navigate(['/home'], { relativeTo: this._route }).then();
  }

  private handleLoginError(error: any) {
    const errorMessage = error.errors || error.message || '登录系统失败，请检查您的用户名和密码';
    this._message.error(errorMessage, {
      autohide: true,
      animation: true,
      delay: 5000,
    });
  }

  private isTokenExpired(authentication: Authentication): boolean {
    // 若无认证信息或无 token 则视为已过期（保守策略）
    if (!authentication || !authentication.token) {
      return true;
    }

    const expires = Number(authentication.expires);
    const lastAccess = Number(authentication.lastAccessTime);

    // 若无法解析为有效数字，则视为已过期
    if (!isFinite(expires) || !isFinite(lastAccess) || expires <= 0) {
      return true;
    }

    // 当前时间（秒）
    const nowSec = Math.floor(Date.now() / 1000);
    const expiryTime = lastAccess + expires;

    // 已过期则返回 true，否则返回 false
    return nowSec >= expiryTime;
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }
}

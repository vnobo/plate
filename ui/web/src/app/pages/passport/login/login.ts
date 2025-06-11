import { afterNextRender, Component, ElementRef, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import {
  debounceTime,
  distinctUntilChanged,
  Observable,
  retry,
  Subject,
  takeUntil,
  tap,
} from 'rxjs';

import { CommonModule } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import dayjs from 'dayjs';
import { Authentication, Credentials } from 'typings';
import { TokenService } from '@app/core/services/token.service';
import { BrowserStorage } from '@app/core';
import { AlertService, ToastService } from '@app/plugins';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule, RouterModule, CommonModule],
  templateUrl: './login.html',
  styleUrl: './login.scss',
})
export class Login {
  private readonly storageKey = 'credentials';

  passwordFieldTextType = signal(false);
  // 使用Signal管理提交状态
  isSubmitting = signal(false);

  // 用于防抖处理的Subject
  private submitSubject = new Subject<void>();
  // 用于取消订阅的Subject
  private destroy$ = new Subject<void>();

  private readonly _tokenSer = inject(TokenService);
  private readonly _storage = inject(BrowserStorage);
  private readonly _toastService = inject(ToastService);
  private readonly _alertService = inject(AlertService);

  loginForm = new FormGroup({
    username: new FormControl('', {
      validators: [Validators.required, Validators.minLength(5), Validators.maxLength(32)],
      nonNullable: true,
    }),
    password: new FormControl('', {
      validators: [Validators.required, Validators.minLength(6), Validators.maxLength(32)],
      nonNullable: true,
    }),
    remember: new FormControl(false),
  });

  constructor(
    private _el: ElementRef,
    private _http: HttpClient,
    private _router: Router,
    private _route: ActivatedRoute,
  ) {
    afterNextRender(() => {
      // 设置防抖提交处理
      this.submitSubject
        .pipe(
          debounceTime(300), // 300ms内的多次提交只会执行一次
          takeUntil(this.destroy$),
        )
        .subscribe(() => {
          this.processLogin();
        });
    });
  }

  // 组件销毁时清理资源
  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  onSubmit() {
    if (this.loginForm.invalid || this.isSubmitting()) {
      return;
    }

    // 触发防抖提交
    this.submitSubject.next();
  }

  // 处理登录逻辑
  private processLogin() {
    // 设置提交状态为true
    this.isSubmitting.set(true);
    try {
      const credentials = this.loginForm.getRawValue();

      if (credentials.remember) {
        this.rememberMe(credentials);
      }

      // 表单验证提示
      if (
        this.loginForm.get('username')?.hasError('required') ||
        this.loginForm.get('password')?.hasError('required')
      ) {
        this._toastService.warning('验证失败', '用户名和密码不能为空', {
          autohide: true,
          delay: 3000,
        });
        this.isSubmitting.set(false);
        return;
      }

      if (
        this.loginForm.get('username')?.hasError('minlength') ||
        this.loginForm.get('username')?.hasError('maxlength')
      ) {
        this._toastService.warning('验证失败', '用户名长度必须在5-32个字符之间', {
          autohide: true,
          delay: 3000,
        });
        this.isSubmitting.set(false);
        return;
      }

      if (
        this.loginForm.get('password')?.hasError('minlength') ||
        this.loginForm.get('password')?.hasError('maxlength')
      ) {
        this._toastService.warning('验证失败', '密码长度必须在6-32个字符之间', {
          autohide: true,
          delay: 3000,
        });
        this.isSubmitting.set(false);
        return;
      }

      this.login(credentials).subscribe({
        error: error => {
          this.handleLoginError(error);
          this.isSubmitting.set(false);
        },
        complete: () => this.isSubmitting.set(false),
      });
    } catch (error) {
      console.error('登录过程中发生错误: ', error);
      this._toastService.error('系统错误', '登录过程中发生未知错误，请稍后重试', {
        autohide: true,
        delay: 5000,
      });
      // 确保即使出错也重置提交状态
      this.isSubmitting.set(false);
    }
  }

  showPassword() {
    this.passwordFieldTextType.set(!this.passwordFieldTextType());
  }

  private login(credentials: Credentials) {
    const headers = new HttpHeaders({
      authorization: 'Basic ' + btoa(credentials.username + ':' + credentials.password),
    });
    return this._http.get<Authentication>('/sec/v1/oauth2/login', { headers: headers }).pipe(
      debounceTime(300),
      distinctUntilChanged(),
      retry({ count: 3, delay: 1000 }),
      takeUntil(this.destroy$),
      tap({
        next: authentication => {
          this._tokenSer.login(authentication);
          this.handleLoginSuccess(authentication);
        },
        error: error => this.handleLoginError(error),
      }),
    );
  }

  private rememberMe(credentials: Credentials) {
    let crstr = JSON.stringify(credentials);
    crstr = btoa(crstr);
    this._storage.setItem(this.storageKey, crstr);
  }

  private handleLoginSuccess(authentication: Authentication) {
    this._toastService.success(
      '登录成功',
      `欢迎 ${authentication.user?.name || '用户'} 登录系统！`,
      {
        autohide: true,
        delay: 3000,
      },
    );
    this._router.navigate(['/home'], { relativeTo: this._route }).then();
  }

  private handleLoginError(error: any) {
    // 清除可能存在的登录信息
    this._tokenSer.clear();
    this._storage.remove(this.storageKey);

    this._toastService.error(
      '登录失败',
      error.errors || error.message || '登录系统失败，请检查您的用户名和密码',
      {
        autohide: true,
        delay: 5000,
      },
    );
  }
}

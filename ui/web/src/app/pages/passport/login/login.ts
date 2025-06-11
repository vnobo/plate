import { afterNextRender, Component, ElementRef, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
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

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule, RouterModule, CommonModule],
  templateUrl: './login.html',
  styleUrl: './login.scss',
})
export class Login {
  passwordFieldTextType = signal(false);
  // 使用Signal管理提交状态
  isSubmitting = signal(false);

  // 用于防抖处理的Subject
  private submitSubject = new Subject<void>();
  // 用于取消订阅的Subject
  private destroy$ = new Subject<void>();

  private readonly _tokenSer = inject(TokenService);
  private readonly _storage = inject(BrowserStorage);

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

  constructor(private _el: ElementRef, private _http: HttpClient) {
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

      this.login(credentials).subscribe({
        next: res => console.log('登录成功: ', res),
        error: err => console.error('登录失败: ', err),
        complete: () => this.isSubmitting.set(false),
      });
    } catch (error) {
      console.error('登录过程中发生错误: ', error);
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
      tap(authentication => this._tokenSer.login(authentication)),
    );
  }
  private rememberMe(credentials: Credentials) {
    let crstr = JSON.stringify(credentials);
    crstr = btoa(crstr);
    this._storage.setItem('credentials', crstr);
  }
}

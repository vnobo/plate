import { CommonModule } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import {
  afterNextRender,
  ChangeDetectionStrategy,
  Component,
  inject,
  OnDestroy,
  signal,
  OnInit, // 添加 OnInit
  AfterViewInit, // 添加 AfterViewInit
} from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { BrowserStorage, TokenService } from '@app/core';
import { MessageService, ModalsService } from '@app/plugins';
import { Authentication, Credentials } from '@plate/types';
import { debounceTime, distinctUntilChanged, retry, Subject, takeUntil, tap } from 'rxjs';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule, RouterModule, CommonModule],
  templateUrl: './login.html',
  styleUrl: './login.scss',
})
export class Login implements OnInit, AfterViewInit, OnDestroy {
  private readonly storageKey = 'credentials';

  private readonly _tokenSer = inject(TokenService);
  private readonly _storage = inject(BrowserStorage);
  private readonly _message = inject(MessageService);
  private readonly _modal = inject(ModalsService);
  private readonly _http = inject(HttpClient);
  private readonly _router = inject(Router);
  private readonly _route = inject(ActivatedRoute);

  private submitSubject = new Subject<void>();
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
    remember: new FormControl(false),
  });

  constructor() {
    afterNextRender(() => {
      this.submitSubject
        .pipe(debounceTime(300), takeUntil(this.destroy$))
        .subscribe(() => this.processLogin());
    });
  }

  ngOnInit(): void {}

  ngAfterViewInit(): void {}

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

    this.submitSubject.next();
  }

  private processLogin() {
    this.isSubmitting.set(true);
    try {
      const credentials = this.loginForm.getRawValue();

      if (credentials.remember) {
        this.rememberMe(credentials);
      }

      this.login(credentials).subscribe({
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

  private login(credentials: Credentials) {
    const headers = new HttpHeaders({
      authorization: 'Basic ' + btoa(credentials.username + ':' + credentials.password),
    });
    return this._http.get<Authentication>('/sec/v1/oauth2/login', { headers: headers }).pipe(
      debounceTime(300),
      distinctUntilChanged(),
      retry({ count: 3, delay: 1000 }),
      takeUntil(this.destroy$),
      tap(authentication => {
        this._tokenSer.login(authentication);
        this.handleLoginSuccess(authentication);
      }),
    );
  }

  private rememberMe(credentials: Credentials) {
    let crstr = JSON.stringify(credentials);
    crstr = btoa(crstr);
    this._storage.setItem(this.storageKey, crstr);
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

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }
}

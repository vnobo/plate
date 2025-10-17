import { CommonModule } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { afterNextRender, Component, inject, OnDestroy, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { TokenService, BrowserStorage } from '@app/core';
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
  private readonly _storage = inject(BrowserStorage);

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
    rememberMe: new FormControl(false, {
      nonNullable: true,
    }),
  });

  constructor() {
    afterNextRender(() => {
      this.submitSubject$
        .pipe(debounceTime(300), takeUntil(this.destroy$))
        .subscribe(() => this.formProcessLogin());
      this.processLogin();
      this.loadRememberedCredentials();
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

  private formProcessLogin() {
    this.isSubmitting.set(true);
    try {
      const credentials = this.loginForm.getRawValue();
      const headers = new HttpHeaders({
        authorization: 'Basic ' + btoa(credentials.username + ':' + credentials.password),
      });
      this.login(headers).subscribe({
        next: authentication => {
          // If "Remember Me" is checked, store credentials
          if (credentials.rememberMe) {
            this.storeCredentials(credentials);
          } else {
            this.clearStoredCredentials();
          }
          this.handleLoginSuccess(authentication);
        },
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
        delay: 1000,
      });
      this.isSubmitting.set(false);
    }
  }

  private processLogin() {
    const headers = new HttpHeaders({ 'x-requested-token': 'none-token-auto-login' });
    this.login(headers)
      .pipe(debounceTime(300), distinctUntilChanged(), takeUntil(this.destroy$))
      .subscribe(authentication => this.handleLoginSuccess(authentication));
  }

  private loadRememberedCredentials() {
    const storedCredentials = this._storage.getItem('credentials');
    if (storedCredentials) {
      try {
        const credentials = JSON.parse(atob(storedCredentials));
        this.loginForm.patchValue({
          username: credentials.username,
          password: credentials.password,
          rememberMe: false,
        });
      } catch (e) {
        // If parsing fails, clear the stored credentials
        this.clearStoredCredentials();
      }
    }
  }

  private storeCredentials(credentials: any) {
    // Store credentials in localStorage with base64 encoding for security
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

  showPassword() {
    this.passwordFieldTextType.set(!this.passwordFieldTextType());
  }

  private login(headers: HttpHeaders) {
    return this._http.get<Authentication>('/sec/oauth2/login', { headers: headers }).pipe(
      debounceTime(300),
      distinctUntilChanged(),
      takeUntil(this.destroy$),
      tap(authentication => this._tokenSer.login(authentication)),
      retry(3),
    );
  }

  private handleLoginSuccess(authentication: Authentication) {
    this._message.success('登录成功, 欢迎 ' + (authentication.details?.nickname as string) + '!', {
      autohide: true,
      delay: 5000,
      animation: true,
    });
    this._router.navigate([this._tokenSer.redirectUrl], { relativeTo: this._route }).then();
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

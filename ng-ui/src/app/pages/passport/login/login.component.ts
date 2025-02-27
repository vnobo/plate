import { AfterViewInit, Component, ElementRef, inject, OnDestroy, OnInit } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { NzNotificationService } from 'ng-zorro-antd/notification';
import { debounceTime, distinctUntilChanged, finalize, retry, Subject, takeUntil } from 'rxjs';
import { Credentials } from '@app/core/types';
import { SHARED_IMPORTS } from '@app/shared/shared-imports';
import { LoginService } from '@app/pages';

@Component({
  selector: 'app-login',
  imports: [...SHARED_IMPORTS],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss'],
})
export class LoginComponent implements OnInit, AfterViewInit, OnDestroy {
  private readonly _router = inject(Router);
  private readonly _route = inject(ActivatedRoute);
  private readonly _el = inject(ElementRef);
  private readonly _message = inject(NzNotificationService);
  private readonly _loginSer = inject(LoginService);

  /** 是否正在提交表单 */
  isSubmitting = false;

  /** 密码字段是否显示为文本 */
  passwordFieldTextType = false;

  /** 用于管理组件销毁时的订阅清理 */
  private readonly destroy$ = new Subject<void>();

  /** 登录表单配置 */
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

  ngAfterViewInit(): void {
    this.initializeTooltips();
  }

  /** 初始化工具提示 */
  private initializeTooltips(): void {
    const tooltipTriggerList = Array.from(this._el.nativeElement.querySelectorAll('[data-bs-toggle="tooltip"]'));
    tooltipTriggerList.forEach((tooltipTriggerEl: any) => {
      const options = {
        delay: { show: 50, hide: 50 },
        html: tooltipTriggerEl.getAttribute('data-bs-html') === 'true',
        placement: tooltipTriggerEl.getAttribute('data-bs-placement') ?? 'auto',
      };
      return new bootstrap.Tooltip(tooltipTriggerEl, options);
    });
  }

  /** 处理表单提交 */
  onSubmit(): void {
    if (this.loginForm.invalid || this.isSubmitting) return;

    this.isSubmitting = true;
    const credentials = this.loginForm.getRawValue();

    if (credentials.remember) {
      this._loginSer.setRememberMe(credentials);
    }

    this.login(credentials);
  }

  ngOnInit(): void {
    this.handleAutoLogin();
  }

  /** 处理自动登录逻辑 */
  private handleAutoLogin(): void {
    const auth = this._loginSer.autoLogin();
    if (auth) {
      const redirectUrl = this._loginSer._auth.redirectUrl || '/home';
      this._router.navigate([redirectUrl], { relativeTo: this._route }).then();
      return;
    }

    const savedCredentials = this._loginSer.getRememberMe();
    if (savedCredentials && Object.keys(savedCredentials).length > 0) {
      this.login(savedCredentials);
    }
  }

  /** 执行登录操作 */
  private login(credentials: Credentials): void {
    this._loginSer
      .login(credentials)
      .pipe(
        debounceTime(300),
        distinctUntilChanged(),
        retry({ count: 3, delay: 1000 }),
        takeUntil(this.destroy$),
        finalize(() => (this.isSubmitting = false)),
      )
      .subscribe({
        next: res => this.handleLoginSuccess(res),
        error: err => this.handleLoginError(err),
      });
  }

  /** 处理登录成功 */
  private handleLoginSuccess(res: any): void {
    this._message.success('登录系统成功', `欢迎 ${res.details.name} 登录系统!`, {
      nzDuration: 3000,
    });
    this._router.navigate(['/home'], { relativeTo: this._route }).then();
  }

  /** 处理登录错误 */
  private handleLoginError(err: any): void {
    this._message.error('登录系统失败', err.errors ?? '未知错误', {
      nzDuration: 5000,
    });
    this._loginSer.logout();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}

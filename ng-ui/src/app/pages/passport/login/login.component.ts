import {AfterViewInit, Component, ElementRef, inject, OnDestroy, OnInit} from '@angular/core';
import {FormControl, FormGroup, Validators} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {NzNotificationService} from 'ng-zorro-antd/notification';
import {debounceTime, distinctUntilChanged, finalize, retry, Subject, takeUntil} from 'rxjs';
import {Credentials} from '@app/core/types';
import {SHARED_IMPORTS} from '@app/shared/shared-imports';
import {LoginService} from '@app/pages';

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

  isSubmitting = false;

  passwordFieldTextType = false;
  private destroy$ = new Subject<void>();

  loginForm = new FormGroup({
    username: new FormControl('', [Validators.required, Validators.minLength(5), Validators.maxLength(32)]),
    password: new FormControl('', [Validators.required, Validators.minLength(6), Validators.maxLength(32)]),
    remember: new FormControl(false),
  });

  ngAfterViewInit(): void {
    const tooltipTriggerList = [].slice.call(this._el.nativeElement.querySelectorAll('[data-bs-toggle="tooltip"]'));
    tooltipTriggerList.map(function (tooltipTriggerEl: any) {
      let options = {
        delay: { show: 50, hide: 50 },
        html: tooltipTriggerEl.getAttribute('data-bs-html') === 'true',
        placement: tooltipTriggerEl.getAttribute('data-bs-placement') ?? 'auto',
      };
      return new bootstrap.Tooltip(tooltipTriggerEl, options);
    });
  }

  onSubmit(): void {
    if (this.loginForm.invalid || this.isSubmitting) return;
    this.isSubmitting = true;
    const credentials = this.loginForm.value as Credentials;
    if (this.loginForm.value.remember) {
      this._loginSer.setRememberMe(credentials);
    }
    this.login(credentials);
  }

  ngOnInit(): void {
    const auth = this._loginSer.autoLogin();
    if (auth) {
      const redirectUrl = this._loginSer._auth.redirectUrl ? this._loginSer._auth.redirectUrl : '/home';
      this._router.navigate([redirectUrl], { relativeTo: this._route }).then();
      return;
    }
    const credentials = this._loginSer.getRememberMe();
    if (credentials && Object.keys(credentials).length !== 0) {
      this.login(credentials);
    }
  }

  login(credentials: Credentials) {
    this._loginSer
      .login(credentials)
      .pipe(
        debounceTime(300),
        distinctUntilChanged(),
        finalize(() => (this.isSubmitting = false)),
        retry(3),
        takeUntil(this.destroy$),
      )
      .subscribe({
        next: res => {
          this._message.success('登录系统成功', `欢迎 ${res.details.name} 登录系统!`);
          this._router.navigate(['/home'], { relativeTo: this._route }).then();
        },
        error: err => {
          this._message.error(`登录系统失败,请重试`, `${err.errors}`);
          this._loginSer.logout();
        },
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }
}

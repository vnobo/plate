import { Component, inject, OnDestroy, OnInit } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { debounceTime, distinctUntilChanged, Subject, takeUntil } from 'rxjs';
import { NzNotificationService } from 'ng-zorro-antd/notification';
import { LoginService } from './login.service';
import { SHARED_IMPORTS } from '../../shared/ shared-imports';
import { Credentials } from '../../core/types';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [SHARED_IMPORTS],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss'],
})
export class LoginComponent implements OnInit, OnDestroy {
  loginForm = new FormGroup({
    username: new FormControl('', [Validators.required, Validators.minLength(5), Validators.maxLength(32)]),
    password: new FormControl('', [Validators.required, Validators.minLength(6), Validators.maxLength(32)]),
    remember: new FormControl(false),
  });
  private readonly _loginSer = inject(LoginService);
  private readonly _message = inject(NzNotificationService);
  private readonly _router = inject(Router);
  private readonly _route = inject(ActivatedRoute);
  private readonly _subject$: Subject<void> = new Subject<void>();

  onSubmit(): void {
    if (this.loginForm.valid) {
      const credentials = this.loginForm.value as Credentials;
      if (this.loginForm.value.remember) {
        this._loginSer.setRememberMe(credentials);
      }
      this.login(credentials);
    } else {
      this._message.error('请输入正确的用户名和密码', '登录失败!');
    }
  }

  ngOnInit(): void {
    const auth = this._loginSer.autoLogin();
    if (auth) {
      this._router.navigate([this._loginSer._auth.redirectUrl], { relativeTo: this._route }).then();
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
      .pipe(takeUntil(this._subject$), debounceTime(100), distinctUntilChanged())
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
    this._subject$.next();
    this._subject$.complete();
  }
}

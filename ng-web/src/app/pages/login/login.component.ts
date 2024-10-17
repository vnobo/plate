import { Component, inject, OnDestroy, OnInit } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { debounceTime, distinctUntilChanged, Subject, takeUntil } from 'rxjs';
import { Credentials, LoginService } from './login.service';
import { CommonModule } from '@angular/common';
import { NzNotificationModule, NzNotificationService } from 'ng-zorro-antd/notification';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, NzNotificationModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss'],
})
export class LoginComponent implements OnInit, OnDestroy {
  private _subject$: Subject<void> = new Subject<void>();
  private _loginSer = inject(LoginService);
  private _message = inject(NzNotificationService);

  loginForm = new FormGroup({
    username: new FormControl('', [
      Validators.required,
      Validators.minLength(5),
      Validators.maxLength(32),
    ]),
    password: new FormControl('', [
      Validators.required,
      Validators.minLength(6),
      Validators.maxLength(32),
    ]),
    remember: new FormControl(false),
  });

  constructor(private router: Router, private route: ActivatedRoute) {
  }

  onSubmit(): void {
    const credentials = this.loginForm.value as Credentials;
    if (this.loginForm.value.remember) {
      this._loginSer.setRememberMe(credentials);
    }
    this.login(credentials);
  }

  ngOnInit(): void {
    const auth = this._loginSer.autoLogin();
    if (auth) {
      this.router.navigate([this._loginSer._auth.redirectUrl], { relativeTo: this.route }).then();
      return;
    }
    const credentials = this._loginSer.getRememberMe();
    if (credentials && Object.keys(credentials).length !== 0) {
      this.login(credentials);
      this._message.success('自动登录成功', '记住我系统自动登录成功!');
    }
  }

  login(credentials: Credentials) {
    this._loginSer
      .login(credentials)
      .pipe(takeUntil(this._subject$), debounceTime(100), distinctUntilChanged())
      .subscribe(res => this.router.navigate(['/home'], { relativeTo: this.route }).then());
  }

  ngOnDestroy(): void {
    this._subject$.next();
    this._subject$.complete();
  }
}

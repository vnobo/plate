import { Component, inject, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { Credentials, LoginService } from './login.service';
import { ActivatedRoute, Router } from '@angular/router';
import { debounceTime, distinctUntilChanged, Subject, takeUntil } from 'rxjs';
import { NzFormModule } from 'ng-zorro-antd/form';
import { NgIf } from '@angular/common';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule, ReactiveFormsModule, NzFormModule, NgIf],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss'],
})
export class LoginComponent implements OnInit, OnDestroy {
  _loginSer = inject(LoginService);
  loginForm: FormGroup<{
    username: FormControl<string | null>;
    password: FormControl<string | null>;
    remember: FormControl<boolean | null>;
  }>;
  private componentDestroyed$: Subject<void> = new Subject<void>();

  constructor(private router: Router, private route: ActivatedRoute, private formBuilder: FormBuilder) {
    this.loginForm = this.formBuilder.group({
      username: new FormControl('', [Validators.required, Validators.minLength(5), Validators.maxLength(64)]),
      password: new FormControl('', [Validators.required, Validators.minLength(6), Validators.maxLength(64)]),
      remember: new FormControl(false),
    });
  }

  onSubmit(): void {
    const credentials: Credentials = {
      username: this.loginForm.value.username,
      password: this.loginForm.value.password,
    };

    if (this.loginForm.value.remember) {
      this._loginSer.setRememberMe(credentials);
    }
    this.login(credentials);
  }

  ngOnInit(): void {
    const credentials = this._loginSer.getRememberMe();
    if (credentials) {
      if (credentials.username == undefined && credentials.password == undefined) {
        return;
      }
      this.login(credentials);
    }
  }

  ngOnDestroy(): void {
    this.componentDestroyed$.next();
    this.componentDestroyed$.complete();
  }

  login(credentials: Credentials) {
    this._loginSer
      .login(credentials)
      .pipe(takeUntil(this.componentDestroyed$), debounceTime(100), distinctUntilChanged())
      .subscribe({
        next: () => {
          this.router.navigate(['/home'], { relativeTo: this.route }).then();
        },
        error: e => console.log(e),
      });
  }
}

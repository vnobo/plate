import { afterNextRender, Component, ElementRef, OnInit } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterModule } from '@angular/router';

import dayjs from 'dayjs';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule, RouterModule],
  templateUrl: './login.html',
  styleUrl: './login.scss',
})
export class Login {
  loginForm = new FormGroup({
    isSubmitting: new FormControl(false),
    passwordFieldTextType: new FormControl(false),
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

  constructor(private _el: ElementRef) {
    afterNextRender(() => {
      console.log('初始化时间: ', dayjs().format('YYYY-MM-DD HH:mm:ss'));
    });
  }

  onSubmit() {
    console.log('登录表单: ', this.loginForm.value);
  }
}

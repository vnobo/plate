import { CommonModule } from '@angular/common';
import { Component, computed, effect, inject, OnInit, output, signal } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { User } from './user.types';

@Component({
  selector: 'app-user-form',
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <div class="container-fluid">
      <form (ngSubmit)="onSubmit()" [formGroup]="userForm" class="form-wrapper">
        <div class="row mb-3">
          <div class="col-lg-6">
            <label class="form-label" for="username">用&nbsp;&nbsp;户&nbsp;&nbsp;名</label>
            <input
              class="form-control"
              type="text"
              formControlName="username"
              id="username"
              autocomplete="off" />
          </div>
          <div class="col-lg-6">
            <label class="form-label" for="name">
              昵&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;称
            </label>
            <input
              class="form-control"
              type="text"
              formControlName="name"
              id="name"
              autocomplete="off" />
          </div>
        </div>
        @if (created()) {
        <div class="row mb-3">
          <div class="col-lg-6">
            <label class="form-label" for="password">
              密&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;码
            </label>
            <input
              class="form-control"
              type="password"
              formControlName="password"
              id="password"
              autocomplete="off" />
          </div>
          <div class="col-lg-6">
            <label class="form-label" for="confirmPassword">确认密码</label>
            <input
              class="form-control"
              type="password"
              formControlName="confirmPassword"
              id="confirmPassword"
              autocomplete="off" />
          </div>
        </div>
        }
        <div class="row mb-3">
          <div class="col-lg-6">
            <label class="form-label" for="email">电子邮件</label>
            <input
              class="form-control"
              type="email"
              formControlName="email"
              id="email"
              type="email" />
          </div>
          <div class="col-lg-6">
            <label class="form-label" for="phone">
              手&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;机
            </label>
            <input class="form-control" type="text" formControlName="phone" id="phone" />
          </div>
        </div>
        <div class="mb-3">
          <label class="form-label" for="bio">个人简介</label>
          <textarea class="form-control" type="text" formControlName="bio" id="bio"></textarea>
        </div>
        <div class="mb-3 d-flex">
          <button class="btn btn-danger" type="reset">重置表单</button>
          <button
            class="btn btn-primary ms-auto"
            [disabled]="userForm.invalid || (!userForm.touched && !userForm.dirty)"
            type="submit">
            <svg
              xmlns="http://www.w3.org/2000/svg"
              width="24"
              height="24"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
              stroke-linecap="round"
              stroke-linejoin="round"
              class="icon icon-1">
              <path d="M12 5l0 14" />
              <path d="M5 12l14 0" />
            </svg>
            保存用户信息
          </button>
        </div>
      </form>
    </div>
  `,
  styles: [
    `
      :host {
        min-height: 100%;
        min-width: 100%;
      }
    `,
  ],
})
export class UserForm implements OnInit {
  userData = signal<User>({} as User);
  created = computed(() => this.userData().code == undefined);
  formSubmit = output<User>();
  private readonly fb = inject(FormBuilder);
  userForm: FormGroup = this.fb.group({
    id: [null],
    code: [''],
    tenantCode: [''],
    username: ['', [Validators.required]],
    password: [''],
    confirmPassword: [''],
    disabled: [false],
    accountExpired: [false],
    accountLocked: [false],
    credentialsExpired: [false],
    email: ['', [Validators.email]],
    phone: [''],
    name: ['', Validators.required],
    avatar: [''],
    bio: [''],
  });

  constructor() {
    effect(() => {
      if (this.created()) {
        this.userForm.controls['password'].addValidators([
          Validators.required,
          Validators.minLength(6),
        ]);
        this.userForm.controls['confirmPassword'].addValidators([
          Validators.required,
          Validators.minLength(6),
        ]);
        this.userForm.patchValue({} as User);
      } else {
        this.userForm.controls['password'].clearValidators();
        this.userForm.controls['confirmPassword'].clearValidators();
        this.userForm.controls['username'].disable({ onlySelf: true });
        this.userForm.patchValue(this.userData());
      }
    });
  }

  ngOnInit(): void {}

  onSubmit(): void {
    if (this.userForm.valid) {
      if (
        this.userForm.controls['password'].value != this.userForm.controls['confirmPassword'].value
      ) {
        this.userForm.controls['confirmPassword'].setErrors({ confirm: true });
        return;
      }
      this.formSubmit.emit(this.userForm.getRawValue());
    }
  }
}

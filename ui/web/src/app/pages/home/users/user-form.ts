import {CommonModule} from '@angular/common';
import {Component, computed, effect, inject, OnInit, output, signal} from '@angular/core';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {User} from './user.types';

@Component({
  selector: 'app-user-form',
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <div class="container">
      <form (ngSubmit)="onSubmit()" [formGroup]="userForm" class="form-wrapper">
        <div class="mb-3">
          <label class="form-label" for="username">用&nbsp;&nbsp;户&nbsp;&nbsp;名</label>
          <input class="form-control" type="text" formControlName="username" id="username" />
        </div>
        @if (created()) {
        <div class="mb-3">
          <label class="form-label" for="password">
            密&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;码
          </label>
          <input class="form-control" type="password" formControlName="password" id="password" />
        </div>
        <div class="mb-3">
          <label class="form-label" for="confirmPassword">确认密码</label>
          <input
            class="form-control"
            type="password"
            formControlName="confirmPassword"
            id="confirmPassword" />
        </div>
        }

        <div class="mb-3">
          <label class="form-label" for="name">
            昵&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;称
          </label>
          <input class="form-control" type="text" formControlName="name" id="name" />
        </div>
        <div class="mb-3">
          <label class="form-label" for="avatar">
            头&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;像
          </label>
          <input class="form-control" type="url" formControlName="avatar" id="avatar" />
        </div>

        <div class="mb-3">
          <label class="form-label" for="email">电子邮件</label>
          <input
            class="form-control"
            type="email"
            formControlName="email"
            id="email"
            type="email" />
        </div>

        <div class="mb-3">
          <label class="form-label" for="phone">
            手&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;机
          </label>
          <input class="form-control" type="text" formControlName="phone" id="phone" />
        </div>

        <div class="mb-3">
          <label class="form-label" for="bio">个人简介</label>
          <textarea class="form-control" type="text" formControlName="bio" id="bio"></textarea>
        </div>
        <div class="mb-3">
          <button class="btn btn-danger" type="reset">重置</button>
          <button
            class="btn btn-primary me-auto"
            [disabled]="userForm.invalid || (!userForm.touched && !userForm.dirty)"
            type="submit">
            保存
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

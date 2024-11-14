import { Component, computed, effect, inject, OnInit, output, signal } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { SHARED_IMPORTS } from '@app/shared/shared-imports';
import { User } from '@app/pages/home/users/user.types';
import { NzModalModule } from 'ng-zorro-antd/modal';

@Component({
  selector: 'app-user-form',
  standalone: true,
  imports: [NzModalModule, ...SHARED_IMPORTS],
  template: `
    <form (ngSubmit)="onSubmit()" [formGroup]="userForm" class="form-wrapper" nz-form>
      <nz-form-item>
        <nz-form-label [nzSpan]="6" nzFor="username" nzRequired>用&nbsp;&nbsp;户&nbsp;&nbsp;名</nz-form-label>
        <nz-form-control [nzSpan]="14" nzErrorTip="Please input your username!">
          <input formControlName="username" id="username" nz-input />
        </nz-form-control>
      </nz-form-item>
      @if (created()) {
        <nz-form-item>
          <nz-form-label [nzSpan]="6" nzFor="password" nzRequired>密&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;码
          </nz-form-label>
          <nz-form-control [nzSpan]="14" nzErrorTip="Password must be at least 6 characters!">
            <input nz-input type="password" formControlName="password" id="password" />
          </nz-form-control>
        </nz-form-item>
        <nz-form-item>
          <nz-form-label [nzSpan]="6" nzFor="confirmPassword" nzRequired>确认密码</nz-form-label>
          <nz-form-control [nzSpan]="14" nzErrorTip="Password must be at least 6 characters!">
            <input nz-input type="password" formControlName="confirmPassword" id="confirmPassword" />
          </nz-form-control>
        </nz-form-item>
      }

      <nz-form-item>
        <nz-form-label [nzSpan]="6" nzFor="name" nzRequired>昵&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;称
        </nz-form-label>
        <nz-form-control [nzSpan]="14" nzErrorTip="Please input your name!">
          <input formControlName="name" id="name" nz-input />
        </nz-form-control>
      </nz-form-item>
      <nz-form-item>
        <nz-form-label [nzSpan]="6" nzFor="avatar">
          头&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;像
        </nz-form-label>
        <nz-form-control [nzSpan]="14">
          <input formControlName="avatar" id="avatar" nz-input />
        </nz-form-control>
      </nz-form-item>

      <nz-form-item>
        <nz-form-label [nzSpan]="6" nzFor="email">电子邮件</nz-form-label>
        <nz-form-control [nzSpan]="14">
          <input formControlName="email" id="email" nz-input type="email" />
        </nz-form-control>
      </nz-form-item>

      <nz-form-item>
        <nz-form-label [nzSpan]="6" nzFor="phone">手&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;机</nz-form-label>
        <nz-form-control [nzSpan]="14">
          <input formControlName="phone" id="phone" nz-input />
        </nz-form-control>
      </nz-form-item>

      <nz-form-item>
        <nz-form-label [nzSpan]="6" nzFor="bio">个人简介</nz-form-label>
        <nz-form-control [nzSpan]="14">
          <textarea formControlName="bio" id="bio" nz-input></textarea>
        </nz-form-control>
      </nz-form-item>
      <nz-form-item>
        <nz-form-control [nzOffset]="6" [nzSpan]="5">
          <button nz-button type="reset">重置</button>
        </nz-form-control>
        <nz-form-control [nzOffset]="3" [nzSpan]="5">
          <button [disabled]="userForm.invalid" nz-button nzType="primary" type="submit">保存</button>
        </nz-form-control>
      </nz-form-item>
    </form>
  `,
  styles: [`:host {
    min-height: 100%;
    min-width: 100%;
  }
  `],
})
export class UserFormComponent implements OnInit {
  userData = signal<User>({} as User);
  created = computed(() => this.userData().id == undefined);
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
        this.userForm.controls['password'].addValidators([Validators.required, Validators.minLength(6)]);
        this.userForm.controls['confirmPassword'].addValidators([Validators.required, Validators.minLength(6)]);
        this.userForm.patchValue({} as User);
      } else {
        this.userForm.controls['password'].clearValidators();
        this.userForm.controls['confirmPassword'].clearValidators();
        this.userForm.controls['username'].disable({ onlySelf: true });
        this.userForm.patchValue(this.userData());
      }
    });
  }

  ngOnInit(): void {
  }

  onSubmit(): void {
    if (this.userForm.valid) {
      if (this.userForm.controls['password'].value != this.userForm.controls['confirmPassword'].value) {
        this.userForm.controls['confirmPassword'].setErrors({ confirm: true });
        return;
      }
      this.formSubmit.emit(this.userForm.getRawValue());
    }
  }
}

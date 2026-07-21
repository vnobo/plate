import {
  Component,
  computed,
  DestroyRef,
  effect,
  inject,
  input,
  linkedSignal,
  signal,
} from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { FormField, form, email, required, submit, disabled } from '@angular/forms/signals';
import { MessageService } from '@app/plugins';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { delay, tap } from 'rxjs';
import { User } from './user.types';
import { environment } from '@envs/env';

@Component({
  selector: 'app-user-form',
  imports: [FormField],
  template: `
    <div class="container-fluid">
      <form (ngSubmit)="onSubmit()" class="form-wrapper">
        <div class="row mb-3">
          <div class="col-lg-6">
            <label class="form-label" for="username">用&nbsp;&nbsp;户&nbsp;&nbsp;名</label>
            <input
              class="form-control"
              type="text"
              id="username"
              autocomplete="off"
              [formField]="userForm.username"
            />
          </div>
          <div class="col-lg-6">
            <label class="form-label" for="name">
              昵&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;称
            </label>
            <input
              class="form-control"
              type="text"
              id="name"
              autocomplete="off"
              [formField]="userForm.name"
            />
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
                id="password"
                autocomplete="off"
                [formField]="userForm.password"
                [class.is-invalid]="passwordError()"
              />
              @if (passwordError()) {
                <div class="invalid-feedback">{{ passwordError() }}</div>
              }
            </div>
            <div class="col-lg-6">
              <label class="form-label" for="confirmPassword">确认密码</label>
              <input
                class="form-control"
                type="password"
                id="confirmPassword"
                autocomplete="off"
                [formField]="userForm.confirmPassword"
                [class.is-invalid]="confirmPasswordError()"
              />
              @if (confirmPasswordError()) {
                <div class="invalid-feedback">{{ confirmPasswordError() }}</div>
              }
            </div>
          </div>
        }
        <div class="row mb-3">
          <div class="col-lg-6">
            <label class="form-label" for="email">电子邮件</label>
            <input class="form-control" type="email" id="email" [formField]="userForm.email" />
          </div>
          <div class="col-lg-6">
            <label class="form-label" for="phone">
              手&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;机
            </label>
            <input class="form-control" type="text" id="phone" [formField]="userForm.phone" />
          </div>
        </div>
        <div class="mb-3">
          <label class="form-label" for="bio">个人简介</label>
          <textarea class="form-control" type="text" id="bio" [formField]="userForm.bio"></textarea>
        </div>
        <div class="mb-3 d-flex">
          <button class="btn btn-danger" type="button" (click)="resetForm()">重置表单</button>
          <button
            class="btn btn-primary ms-auto"
            [disabled]="!userForm().valid() || isSubmitting()"
            type="submit"
          >
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
              class="icon icon-1"
            >
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
export class UserForm {
  inputData = input<User | null>(null);

  private readonly userData = linkedSignal(this.inputData);
  readonly created = computed(() => this.userData()?.code == undefined);

  private readonly _http = inject(HttpClient);
  private readonly _message = inject(MessageService);
  private readonly _destroyRef = inject(DestroyRef);

  isSubmitting = signal(false);
  passwordError = signal('');
  confirmPasswordError = signal('');

  private readonly initialModel = {
    id: undefined as number | undefined,
    code: '',
    tenantCode: '',
    username: '',
    password: '',
    confirmPassword: '',
    disabled: false,
    accountExpired: false,
    accountLocked: false,
    credentialsExpired: false,
    email: '',
    phone: '',
    name: '',
    avatar: '',
    bio: '',
  };

  protected readonly userModel = signal({ ...this.initialModel });

  protected readonly userForm = form(this.userModel, (p) => {
    required(p.username, { message: '用户名 是必填项' });
    disabled(p.username, () => !this.created());
    required(p.name, { message: '昵称 是必填项' });
    email(p.email, { message: '电子邮件 格式不正确' });
  });

  constructor() {
    effect(() => {
      const data = this.userData();
      if (this.created()) {
        this.userModel.set({ ...this.initialModel });
      } else if (data) {
        this.userModel.set({
          ...this.initialModel,
          ...data,
          password: '',
          confirmPassword: '',
        });
      }
    });
  }

  async onSubmit() {
    this.passwordError.set('');
    this.confirmPasswordError.set('');

    if (this.created()) {
      const model = this.userModel();
      if (!model.password || model.password.length < 6) {
        this.passwordError.set(model.password ? '密码至少6个字符' : '密码 是必填项');
        return;
      }
      if (!model.confirmPassword || model.confirmPassword.length < 6) {
        this.confirmPasswordError.set(
          model.confirmPassword ? '确认密码至少6个字符' : '确认密码 是必填项',
        );
        return;
      }
      if (model.password !== model.confirmPassword) {
        this.confirmPasswordError.set('两次输入的密码不一致');
        return;
      }
    }

    this.isSubmitting.set(true);
    await submit(this.userForm, {
      action: async () => {
        const model = this.userModel();
        const result: User = { ...model } as User;
        // Do not submit the password on edit, to avoid accidentally changing it
        if (!this.created()) {
          delete (result as Record<string, unknown>)['password'];
          delete (result as Record<string, unknown>)['confirmPassword'];
        }
        const request = this.created()
          ? this._http.post<User>(environment.secApiPath + '/users/add', result)
          : this._http.put<User>(environment.secApiPath + '/users/modify', result);
        request
          .pipe(
            tap(() =>
              this._message.success(this.created() ? '用户创建成功' : '用户更新成功'),
            ),
            delay(800),
            takeUntilDestroyed(this._destroyRef),
          )
          .subscribe(() => this.closeModal());
      },
    });
    this.isSubmitting.set(false);
  }

  private closeModal() {
    const el = document.getElementById('exampleModal');
    if (el) {
      const instance = tabler?.Modal?.getOrCreateInstance(el);
      instance?.hide();
    }
  }

  resetForm() {
    this.userModel.set({ ...this.initialModel });
    this.passwordError.set('');
    this.confirmPasswordError.set('');
  }
}

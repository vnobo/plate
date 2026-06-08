import { Component, inject, signal, computed, afterNextRender, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { NgOptimizedImage } from '@angular/common';
import { AuthService } from '../../core/auth.service';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule, NgOptimizedImage],
  templateUrl: './login.html',
  styleUrl: './login.scss',
})
export class Login implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly router = inject(Router);
  private readonly authService = inject(AuthService);

  readonly loginForm: FormGroup;

  readonly loginError = signal('');
  readonly isLoading = computed(() => this.authService.isLoading());
  readonly isSubmitting = signal(false);
  readonly showPassword = signal(false);

  constructor() {
    this.loginForm = this.fb.group({
      username: ['', [Validators.required, Validators.minLength(3)]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      rememberMe: [false],
    });

    // 加载记住的用户名
    afterNextRender(() => {
      const remembered = this.authService.getRememberedUsername();
      if (remembered) {
        this.loginForm.patchValue({
          username: remembered,
          rememberMe: true,
        });
      }
    });
  }

  async ngOnInit(): Promise<void> {
    // 已登录用户重定向到首页
    if (this.authService.isAuthenticated()) {
      await this.router.navigate(['/']);
    }
  }

  async onSubmit(): Promise<void> {
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    this.isSubmitting.set(true);
    this.loginError.set('');

    const result = await this.authService.login(this.loginForm.value);

    this.isSubmitting.set(false);

    if (result.success) {
      await this.router.navigate(['/']);
    } else {
      this.loginError.set(result.error ?? '登录失败，请重试');
    }
  }

  togglePasswordVisibility(): void {
    this.showPassword.update((v) => !v);
  }

  // 字段错误辅助方法
  getFieldError(field: string): string {
    const control = this.loginForm.get(field);
    if (!control || !control.touched || control.valid) return '';

    if (control.hasError('required')) return '此项为必填';
    if (control.hasError('minlength')) {
      const min = control.getError('minlength')?.requiredLength ?? 0;
      return `最少输入 ${min} 个字符`;
    }
    return '输入无效';
  }
}

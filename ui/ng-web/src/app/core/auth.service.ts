import { Injectable, inject, signal, computed } from '@angular/core';
import { Router } from '@angular/router';

export interface UserInfo {
  id: string;
  username: string;
  email: string;
}

export interface LoginRequest {
  username: string;
  password: string;
  rememberMe: boolean;
}

export interface LoginResult {
  success: boolean;
  error?: string;
  user?: UserInfo;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly router = inject(Router);

  readonly currentUser = signal<UserInfo | null>(null);
  readonly isAuthenticated = computed(() => this.currentUser() !== null);
  readonly isLoading = signal(false);

  async login(request: LoginRequest): Promise<LoginResult> {
    this.isLoading.set(true);

    try {
      // TODO: 替换为实际 API 调用
      const result = await this.mockLogin(request);

      if (result.success && result.user) {
        this.currentUser.set(result.user);
        if (request.rememberMe) {
          localStorage.setItem('remembered_user', request.username);
        } else {
          localStorage.removeItem('remembered_user');
        }
      }

      return result;
    } finally {
      this.isLoading.set(false);
    }
  }

  async logout(): Promise<void> {
    this.currentUser.set(null);
    await this.router.navigate(['/login']);
  }

  getRememberedUsername(): string {
    return localStorage.getItem('remembered_user') ?? '';
  }

  private async mockLogin(request: LoginRequest): Promise<LoginResult> {
    // 模拟网络延迟
    await new Promise((resolve) => setTimeout(resolve, 800));

    if (request.username === 'admin' && request.password === 'admin') {
      return {
        success: true,
        user: {
          id: '1',
          username: 'admin',
          email: 'admin@example.com',
        },
      };
    }

    return {
      success: false,
      error: '用户名或密码不正确',
    };
  }
}

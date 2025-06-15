import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { provideRouter } from '@angular/router';
import { Login } from './login';
import { BrowserStorage } from '@app/core';
import { TokenService } from '@app/core/services/token.service';
import { Router } from '@angular/router';
import { MessageService } from '@app/plugins';

describe('Login', () => {
  let component: Login;
  let fixture: ComponentFixture<Login>;
  let httpMock: HttpTestingController;
  let tokenService: jasmine.SpyObj<TokenService>;
  let storageService: jasmine.SpyObj<BrowserStorage>;
  let messageService: jasmine.SpyObj<MessageService>;
  let router: Router;

  beforeEach(async () => {
    // 创建mock服务
    const tokenServiceSpy = jasmine.createSpyObj('TokenService', ['login']);
    const storageServiceSpy = jasmine.createSpyObj('BrowserStorage', ['setItem']);
    const toastServiceSpy = jasmine.createSpyObj('ToastService', ['info', 'error']);

    await TestBed.configureTestingModule({
      imports: [Login, ReactiveFormsModule],
      providers: [
        provideHttpClientTesting(),
        provideRouter([]),
        { provide: TokenService, useValue: tokenServiceSpy },
        { provide: BrowserStorage, useValue: storageServiceSpy },
        { provide: MessageService, useValue: toastServiceSpy },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(Login);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
    tokenService = TestBed.inject(TokenService) as jasmine.SpyObj<TokenService>;
    storageService = TestBed.inject(BrowserStorage) as jasmine.SpyObj<BrowserStorage>;
    messageService = TestBed.inject(MessageService) as jasmine.SpyObj<MessageService>;
    router = TestBed.inject(Router);
    fixture.detectChanges();
  });

  afterEach(() => {
    httpMock.verify(); // 确保没有未处理的请求
  });

  // 1. 基础测试
  describe('Basic Tests', () => {
    it('should create', () => {
      expect(component).toBeTruthy();
    });

    it('should initialize form with empty values', () => {
      expect(component.loginForm.get('username')?.value).toBe('');
      expect(component.loginForm.get('password')?.value).toBe('');
      expect(component.loginForm.get('remember')?.value).toBeFalse();
    });

    it('should inject all required services', () => {
      expect(tokenService).toBeTruthy();
      expect(storageService).toBeTruthy();
      expect(MessageService).toBeTruthy();
    });
  });

  // 2. 表单验证测试
  describe('Form Validation Tests', () => {
    describe('Username Validation', () => {
      it('should require username', () => {
        const usernameControl = component.loginForm.get('username');
        expect(usernameControl?.hasError('required')).toBeTrue();
        usernameControl?.setValue('test');
        expect(usernameControl?.hasError('required')).toBeFalse();
      });

      it('should validate username minimum length', () => {
        const usernameControl = component.loginForm.get('username');
        usernameControl?.setValue('test');
        expect(usernameControl?.hasError('minlength')).toBeTrue();
        usernameControl?.setValue('testuser');
        expect(usernameControl?.hasError('minlength')).toBeFalse();
      });

      it('should validate username maximum length', () => {
        const usernameControl = component.loginForm.get('username');
        const longUsername = 'a'.repeat(33);
        usernameControl?.setValue(longUsername);
        expect(usernameControl?.hasError('maxlength')).toBeTrue();
        usernameControl?.setValue('validusername');
        expect(usernameControl?.hasError('maxlength')).toBeFalse();
      });
    });

    describe('Password Validation', () => {
      it('should require password', () => {
        const passwordControl = component.loginForm.get('password');
        expect(passwordControl?.hasError('required')).toBeTrue();
        passwordControl?.setValue('password123');
        expect(passwordControl?.hasError('required')).toBeFalse();
      });

      it('should validate password minimum length', () => {
        const passwordControl = component.loginForm.get('password');
        passwordControl?.setValue('12345');
        expect(passwordControl?.hasError('minlength')).toBeTrue();
        passwordControl?.setValue('123456');
        expect(passwordControl?.hasError('minlength')).toBeFalse();
      });

      it('should validate password maximum length', () => {
        const passwordControl = component.loginForm.get('password');
        const longPassword = 'a'.repeat(33);
        passwordControl?.setValue(longPassword);
        expect(passwordControl?.hasError('maxlength')).toBeTrue();
        passwordControl?.setValue('validpassword123');
        expect(passwordControl?.hasError('maxlength')).toBeFalse();
      });
    });
  });

  // 3. 功能测试
  describe('Feature Tests', () => {
    it('should toggle password visibility', () => {
      expect(component.passwordFieldTextType()).toBeFalse();
      component.showPassword();
      expect(component.passwordFieldTextType()).toBeTrue();
      component.showPassword();
      expect(component.passwordFieldTextType()).toBeFalse();
    });

    it('should handle remember me functionality', fakeAsync(() => {
      const credentials = {
        username: 'testuser',
        password: 'password123',
        remember: true,
      };
      component.loginForm.patchValue(credentials);
      component.onSubmit();
      tick(300); // 等待防抖

      const expectedCredentialsStr = btoa(JSON.stringify(credentials));
      expect(storageService.setItem).toHaveBeenCalledWith('credentials', expectedCredentialsStr);
    }));

    it('should handle successful login', fakeAsync(() => {
      const mockAuth = {
        token: 'test-token',
        expires: 3600,
        lastAccessTime: Date.now(),
        details: {},
      };

      component.loginForm.patchValue({
        username: 'testuser',
        password: 'password123',
      });

      component.onSubmit();
      tick(300); // 等待防抖

      const req = httpMock.expectOne('/sec/v1/oauth2/login');
      expect(req.request.headers.get('authorization')).toContain('Basic ');
      req.flush(mockAuth);

      expect(tokenService.login).toHaveBeenCalledWith(mockAuth);
      const navigateSpy = spyOn(router, 'navigate');
      expect(navigateSpy).toHaveBeenCalledWith(['/home'], jasmine.any(Object));
    }));
  });

  // 4. 服务交互测试
  describe('Service Interaction Tests', () => {
    it('should make HTTP request with correct headers', fakeAsync(() => {
      component.loginForm.patchValue({
        username: 'testuser',
        password: 'password123',
      });

      component.onSubmit();
      tick(300);

      const req = httpMock.expectOne('/sec/v1/oauth2/login');
      expect(req.request.method).toBe('GET');
      expect(req.request.headers.get('authorization')).toBe(
        'Basic ' + btoa('testuser:password123'),
      );
    }));

    it('should handle login error', fakeAsync(() => {
      component.loginForm.patchValue({
        username: 'testuser',
        password: 'wrongpassword',
      });

      component.onSubmit();
      tick(300);
      //req.error(new ProgressEvent('error'));
      const req = httpMock.expectOne('/sec/v1/oauth2/login');
      req.error(new ProgressEvent('Network error'));

      expect(component.isSubmitting()).toBeFalse();
    }));
  });

  // 5. 边界情况测试
  describe('Edge Cases', () => {
    it('should handle multiple rapid submissions', fakeAsync(() => {
      component.loginForm.patchValue({
        username: 'testuser',
        password: 'password123',
      });

      // 快速连续提交三次
      component.onSubmit();
      component.onSubmit();
      component.onSubmit();

      tick(300); // 等待防抖时间

      // 应该只发出一个请求
      const requests = httpMock.match('/sec/v1/oauth2/login');
      expect(requests.length).toBe(1);
    }));

    it('should not submit if form is invalid', () => {
      component.onSubmit();
      httpMock.expectNone('/sec/v1/oauth2/login');
    });

    it('should not submit while already submitting', fakeAsync(() => {
      component.loginForm.patchValue({
        username: 'testuser',
        password: 'password123',
      });

      component.isSubmitting.set(true);
      component.onSubmit();
      tick(300);

      httpMock.expectNone('/sec/v1/oauth2/login');
    }));
  });
});

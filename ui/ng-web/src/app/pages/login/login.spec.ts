import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideLocationMocks } from '@angular/common/testing';
import { Login } from './login';
import { AuthService } from '../../core/auth.service';

describe('Login', () => {
  let authService: AuthService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Login],
      providers: [provideRouter([{ path: 'login', component: Login }]), provideLocationMocks()],
    }).compileComponents();

    authService = TestBed.inject(AuthService);
  });

  it('should create the component', () => {
    const fixture = TestBed.createComponent(Login);
    const component = fixture.componentInstance;
    expect(component).toBeTruthy();
  });

  describe('Form validation', () => {
    it('should mark form as invalid when empty', () => {
      const fixture = TestBed.createComponent(Login);
      const component = fixture.componentInstance;
      expect(component.loginForm.valid).toBe(false);
    });

    it('should require username with minimum 3 characters', () => {
      const fixture = TestBed.createComponent(Login);
      const component = fixture.componentInstance;

      const username = component.loginForm.get('username');
      username?.setValue('ab');
      expect(username?.valid).toBe(false);
      expect(username?.hasError('minlength')).toBe(true);

      username?.setValue('abc');
      expect(username?.valid).toBe(true);
    });

    it('should require password with minimum 6 characters', () => {
      const fixture = TestBed.createComponent(Login);
      const component = fixture.componentInstance;

      const password = component.loginForm.get('password');
      password?.setValue('12345');
      expect(password?.valid).toBe(false);
      expect(password?.hasError('minlength')).toBe(true);

      password?.setValue('123456');
      expect(password?.valid).toBe(true);
    });

    it('should mark all fields as touched on invalid submit', () => {
      const fixture = TestBed.createComponent(Login);
      const component = fixture.componentInstance;

      component.onSubmit();
      expect(component.loginForm.touched).toBe(true);
    });
  });

  describe('getFieldError', () => {
    it('should return empty string for valid untouched field', () => {
      const fixture = TestBed.createComponent(Login);
      const component = fixture.componentInstance;
      expect(component.getFieldError('username')).toBe('');
    });

    it('should return required error message', () => {
      const fixture = TestBed.createComponent(Login);
      const component = fixture.componentInstance;

      const control = component.loginForm.get('username');
      control?.markAsTouched();
      control?.setErrors({ required: true });
      expect(component.getFieldError('username')).toBe('此项为必填');
    });
  });

  describe('Password visibility', () => {
    it('should toggle password visibility', () => {
      const fixture = TestBed.createComponent(Login);
      const component = fixture.componentInstance;

      expect(component.showPassword()).toBe(false);
      component.togglePasswordVisibility();
      expect(component.showPassword()).toBe(true);
      component.togglePasswordVisibility();
      expect(component.showPassword()).toBe(false);
    });
  });

  describe('Login flow', () => {
    it('should call auth service on valid submit', async () => {
      const fixture = TestBed.createComponent(Login);
      const component = fixture.componentInstance;
      const loginSpy = vi.spyOn(authService, 'login').mockResolvedValue({
        success: true,
        user: { id: '1', username: 'admin', email: 'admin@example.com' },
      });

      component.loginForm.setValue({
        username: 'admin',
        password: 'admin123',
        rememberMe: false,
      });

      await component.onSubmit();

      expect(loginSpy).toHaveBeenCalledWith({
        username: 'admin',
        password: 'admin123',
        rememberMe: false,
      });
    });

    it('should set error message on failed login', async () => {
      const fixture = TestBed.createComponent(Login);
      const component = fixture.componentInstance;

      vi.spyOn(authService, 'login').mockResolvedValue({
        success: false,
        error: '用户名或密码不正确',
      });

      component.loginForm.setValue({
        username: 'wrong',
        password: 'wrongpassword',
        rememberMe: false,
      });

      await component.onSubmit();

      expect(component.loginError()).toBe('用户名或密码不正确');
    });
  });
});

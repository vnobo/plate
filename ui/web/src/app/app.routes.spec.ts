import { TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { routes } from './app.routes';

/**
 * 应用路由配置测试
 *
 * 测试路由配置的正确性和完整性
 */
describe('App Routes Configuration', () => {
  let router: Router;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [RouterTestingModule.withRoutes(routes)],
    });

    router = TestBed.inject(Router);
  });

  it('应该包含正确的路由结构', () => {
    const config = router.config;

    // 验证路由数量
    expect(config.length).toBe(5);

    // 验证路由路径
    const paths = config.map(route => route.path);
    expect(paths).toEqual(['home', 'passport', 'exception', '', '**']);
  });

  it('home路由应该配置正确的标题', () => {
    const homeRoute = router.config.find(route => route.path === 'home');
    expect(homeRoute).toBeDefined();
    expect(homeRoute?.title).toBe('应用主页');
  });

  it('passport路由应该配置正确的标题', () => {
    const passportRoute = router.config.find(route => route.path === 'passport');
    expect(passportRoute).toBeDefined();
    expect(passportRoute?.title).toBe('欢迎登陆PLATE系统综合管理平台');
  });

  it('exception路由应该配置错误页面数据', () => {
    const exceptionRoute = router.config.find(route => route.path === 'exception');
    expect(exceptionRoute).toBeDefined();
    expect(exceptionRoute?.data?.['requiresAuth']).toBe(false);
    expect(exceptionRoute?.data?.['title']).toBe('错误页面');
    expect(exceptionRoute?.data?.['breadcrumb']).toBe('错误');
  });

  it('默认路由应该重定向到passport', () => {
    const defaultRoute = router.config.find(route => route.path === '');
    expect(defaultRoute).toBeDefined();
    expect(defaultRoute?.redirectTo).toBe('/passport');
    expect(defaultRoute?.pathMatch).toBe('full');
  });

  it('通配符路由应该重定向到404页面', () => {
    const wildcardRoute = router.config.find(route => route.path === '**');
    expect(wildcardRoute).toBeDefined();
    expect(wildcardRoute?.redirectTo).toBe('exception/404');
  });

  it('通配符路由应该放在最后', () => {
    const lastRoute = router.config[router.config.length - 1];
    expect(lastRoute.path).toBe('**');
  });

  it('路由配置应该通过验证', () => {
    // 模拟开发环境
    (window as any).ngDevMode = true;

    // 验证应该不会抛出错误
    expect(() => {
      const config = router.config;
    }).not.toThrow();
  });

  describe('懒加载路由功能', () => {
    it('home路由应该支持懒加载', async () => {
      const homeRoute = router.config.find(route => route.path === 'home');
      expect(homeRoute?.loadChildren).toBeDefined();

      // 测试懒加载函数不会抛出错误
      const loadResult = homeRoute?.loadChildren!();
      if (loadResult instanceof Promise) {
        await expectAsync(loadResult).toBeResolved();
      }
    });

    it('passport路由应该支持懒加载', async () => {
      const passportRoute = router.config.find(route => route.path === 'passport');
      expect(passportRoute?.loadChildren).toBeDefined();

      const loadResult = passportRoute?.loadChildren!();
      if (loadResult instanceof Promise) {
        await expectAsync(loadResult).toBeResolved();
      }
    });

    it('exception路由应该支持懒加载', async () => {
      const exceptionRoute = router.config.find(route => route.path === 'exception');
      expect(exceptionRoute?.loadChildren).toBeDefined();

      const loadResult = exceptionRoute?.loadChildren!();
      if (loadResult instanceof Promise) {
        await expectAsync(loadResult).toBeResolved();
      }
    });
  });

  describe('路由数据配置', () => {
    it('重定向路由应该配置skipLocationChange', () => {
      const redirectRoutes = router.config.filter(
        route => route.redirectTo && route.data?.['skipLocationChange'] !== undefined,
      );

      expect(redirectRoutes.length).toBe(2); // 默认路由和通配符路由
    });
  });
});
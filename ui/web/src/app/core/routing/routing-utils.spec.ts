import { Routes } from '@angular/router';
import {
  createLazyRoute,
  createRedirectRoute,
  createWildcardRoute,
  validateRoutes,
  PreloadStrategies,
  getPreloadConfig
} from './routing-utils';

describe('Routing Utils', () => {
  describe('createLazyRoute', () => {
    it('should create a lazy route configuration', () => {
      const importFn = () => Promise.resolve({ default: [] });
      const routeName = 'Test Route';
      
      const result = createLazyRoute(importFn, routeName);
      
      expect(result).toBeDefined();
      expect(typeof result.loadChildren).toBe('function');
    });

    it('should handle successful module loading', async () => {
      const testModule = { default: [] };
      const importFn = () => Promise.resolve(testModule);
      const routeName = 'Test Route';
      
      const lazyRoute = createLazyRoute(importFn, routeName);
      const result = await lazyRoute.loadChildren();
      
      expect(result).toBe(testModule);
    });

    it('should handle module loading errors gracefully', async () => {
      const importFn = () => Promise.reject(new Error('Load failed'));
      const routeName = 'Test Route';
      
      const lazyRoute = createLazyRoute(importFn, routeName);
      const result = await lazyRoute.loadChildren();
      
      expect(result).toEqual({ default: [] });
    });
  });

  describe('createRedirectRoute', () => {
    it('should create a redirect route with default settings', () => {
      const redirectTo = '/home';
      
      const result = createRedirectRoute(redirectTo);
      
      expect(result).toEqual({
        path: '',
        pathMatch: 'full',
        redirectTo,
        data: { skipLocationChange: true }
      });
    });

    it('should create a redirect route with custom skipLocationChange setting', () => {
      const redirectTo = '/home';
      const skipLocationChange = false;
      
      const result = createRedirectRoute(redirectTo, skipLocationChange);
      
      expect(result).toEqual({
        path: '',
        pathMatch: 'full',
        redirectTo,
        data: { skipLocationChange }
      });
    });
  });

  describe('createWildcardRoute', () => {
    it('should create a wildcard route with default settings', () => {
      const redirectTo = '404';
      
      const result = createWildcardRoute(redirectTo);
      
      expect(result).toEqual({
        path: '**',
        redirectTo,
        data: { skipLocationChange: true }
      });
    });

    it('should create a wildcard route with custom skipLocationChange setting', () => {
      const redirectTo = '404';
      const skipLocationChange = false;
      
      const result = createWildcardRoute(redirectTo, skipLocationChange);
      
      expect(result).toEqual({
        path: '**',
        redirectTo,
        data: { skipLocationChange }
      });
    });
  });

  describe('validateRoutes', () => {
    it('should validate routes without errors', () => {
      const routes: Routes = [
        { path: 'home' },
        { path: 'about' },
        { path: '', redirectTo: '/home', pathMatch: 'full' },
        { path: '**', redirectTo: '404' }
      ];
      
      const result = validateRoutes(routes);
      
      expect(result.isValid).toBe(true);
      expect(result.errors).toEqual([]);
    });

    it('should detect duplicate paths', () => {
      const routes: Routes = [
        { path: 'home' },
        { path: 'home' }, // Duplicate
        { path: '', redirectTo: '/home', pathMatch: 'full' }
      ];
      
      const result = validateRoutes(routes);
      
      expect(result.isValid).toBe(false);
      expect(result.errors.length).toBe(1);
      expect(result.errors[0]).toContain('重复的路由路径');
    });

    it('should detect wildcard route not at the end', () => {
      const routes: Routes = [
        { path: 'home' },
        { path: '**', redirectTo: '404' }, // Should be last
        { path: 'about' }
      ];
      
      const result = validateRoutes(routes);
      
      expect(result.isValid).toBe(false);
      expect(result.errors.length).toBe(1);
      expect(result.errors[0]).toContain('通配符路由 (**)');
    });
  });

  describe('PreloadStrategies', () => {
    it('should define preload strategies', () => {
      expect(PreloadStrategies.IMMEDIATE).toBe('immediate');
      expect(PreloadStrategies.ON_DEMAND).toBe('on-demand');
      expect(PreloadStrategies.IDLE).toBe('idle');
    });
  });

  describe('getPreloadConfig', () => {
    it('should return default preload config', () => {
      const result = getPreloadConfig();
      
      expect(result).toEqual({
        preloadingStrategy: 'on-demand'
      });
    });

    it('should return preload config with specified strategy', () => {
      const strategy = 'immediate';
      const result = getPreloadConfig(strategy);
      
      expect(result).toEqual({
        preloadingStrategy: strategy
      });
    });
  });
});
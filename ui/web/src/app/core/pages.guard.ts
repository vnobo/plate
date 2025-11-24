import {
  ActivatedRouteSnapshot,
  CanActivateChildFn,
  type CanActivateFn,
  CanDeactivateFn,
  Router,
  RouterStateSnapshot,
} from '@angular/router';
import { inject } from '@angular/core';
import { TokenService } from './services/token.service';

export const canActivateGuard: CanActivateFn = (route, state) => {
  const auth = inject(TokenService);
  const router = inject(Router);
  if (auth.isLogged()) {
    return true;
  }
  auth.redirectUrl = state.url;
  return router.parseUrl(auth.loginUrl);
};

export const roleChildGuard: CanActivateChildFn = (
  childRoute: ActivatedRouteSnapshot,
  state: RouterStateSnapshot,
) => {
  const auth = inject(TokenService);
  // 从路由数据中获取角色信息
  const role = childRoute.data['role'];
  // 如果没有指定角色，则允许访问
  if (!role) {
    return true;
  }
  return auth.hasRole(role);
};

/**
 * Checks if the user has unsaved changes before leaving the form
 *
 * @param component The component being deactivated
 * @param currentRoute The current route
 * @param currentState The current state
 * @param nextState The next state
 * @returns true if the user can leave the form, false otherwise
export const unsavedChangesGuard: CanDeactivateFn<FormComponent> = (
  component: FormComponent,
  currentRoute: ActivatedRouteSnapshot,
  currentState: RouterStateSnapshot,
  nextState: RouterStateSnapshot,
) => {
  return component.hasUnsavedChanges()
    ? confirm('You have unsaved changes. Are you sure you want to leave?')
    : true;
};
 */

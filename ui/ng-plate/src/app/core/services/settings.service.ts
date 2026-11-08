import { Service } from '@angular/core';

@Service()
export class SettingsService {
  /**
   * 切换亮/暗模式。与 tabler-theme.js 的机制保持一致：写 data-bs-theme 属性 +
   * tabler-theme 存储键；存了显式值后，tabler-theme.js 的跟随系统偏好逻辑便不再覆盖。
   */
  setTheme(theme: 'light' | 'dark'): void {
    document.documentElement.setAttribute('data-bs-theme', theme);
    localStorage.setItem('tabler-theme', theme);
  }
}

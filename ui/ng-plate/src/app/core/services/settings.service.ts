import { Service } from '@angular/core';
import { ParamMap } from '@angular/router';

const themeConfig: Record<string, string> = {
  theme: 'light',
  'theme-base': 'gray',
  'theme-font': 'sans-serif',
  'theme-primary': 'blue',
  'theme-radius': '1',
};

@Service()
export class SettingsService {
  setting(params: ParamMap) {
    for (const key in themeConfig) {
      const param = params.get(key);
      let selectedValue;

      if (param) {
        localStorage.setItem('tabler-' + key, param);
        selectedValue = param;
      } else {
        const storedTheme = localStorage.getItem('tabler-' + key);
        selectedValue = storedTheme ?? themeConfig[key];
      }

      if (selectedValue !== themeConfig[key]) {
        document.documentElement.setAttribute('data-bs-' + key, selectedValue);
      } else {
        document.documentElement.removeAttribute('data-bs-' + key);
      }
    }
  }

  /**
   * 切换亮/暗模式。与 tabler-theme.js 的机制保持一致：写 data-bs-theme 属性 +
   * tabler-theme 存储键；存了显式值后，tabler-theme.js 的跟随系统偏好逻辑便不再覆盖。
   */
  setTheme(theme: 'light' | 'dark'): void {
    document.documentElement.setAttribute('data-bs-theme', theme);
    localStorage.setItem('tabler-theme', theme);
  }
}

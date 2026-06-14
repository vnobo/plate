import { Injectable } from '@angular/core';
import { ParamMap } from '@angular/router';

const themeConfig: Record<string, string> = {
  theme: 'light',
  'theme-base': 'gray',
  'theme-font': 'sans-serif',
  'theme-primary': 'blue',
  'theme-radius': '1',
};

@Injectable({ providedIn: 'root' })
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
}

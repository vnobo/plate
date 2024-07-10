import { isPlatformBrowser } from '@angular/common';
import { afterNextRender, Inject, Injectable, PLATFORM_ID } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class BrowserStorageService {
  private storage: Storage = {
    getItem: () => null,
    setItem: () => null,
    removeItem: () => null,
    length: 0,
    key: () => null,
    clear: () => null,
  };
  constructor(@Inject(PLATFORM_ID) private platformId: Object) {
    afterNextRender(() => {
      console.log('Browser storage loaded');
      this.storage = localStorage;
    });
  }

  get(key: string) {
    if (isPlatformBrowser(this.platformId)) {
      const itemStr = localStorage.getItem(key);
      if (itemStr) {
        return atob(itemStr);
      }
    }
    return null;
  }

  set(key: string, value: string) {
    const btoaStr = btoa(value);
    this.storage.setItem(key, btoaStr);
  }

  remove(key: string) {
    this.storage.removeItem(key);
  }

  clear() {
    this.storage.clear();
  }
}

import { isPlatformBrowser } from '@angular/common';
import { Inject, Injectable, PLATFORM_ID } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class BrowserStorageService {
  private cacheMap = new Map<string, any>();

  constructor(@Inject(PLATFORM_ID) private platformId: Object) {}

  get(key: string) {
    if (isPlatformBrowser(this.platformId)) {
      const itemStr = localStorage.getItem(key);
      if (itemStr) {
        return atob(itemStr);
      }
    }
    return this.cacheMap.get(key);
  }

  set(key: string, value: string) {
    const btoaStr = btoa(value);
    if (isPlatformBrowser(this.platformId)) {
      localStorage.setItem(key, btoaStr);
    } else {
      this.cacheMap.set(key, value);
    }
  }

  remove(key: string) {
    if (isPlatformBrowser(this.platformId)) {
      localStorage.removeItem(key);
    } else {
      this.cacheMap.delete(key);
    }
  }

  clear() {
    if (isPlatformBrowser(this.platformId)) {
      localStorage.clear();
    } else {
      this.cacheMap.clear();
    }
  }
}

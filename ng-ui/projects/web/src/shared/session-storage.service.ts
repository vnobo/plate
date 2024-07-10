import { isPlatformBrowser } from '@angular/common';
import { Inject, Injectable, PLATFORM_ID } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class SessionStorageService {
  private cacheMap = new Map<string, any>();

  constructor(@Inject(PLATFORM_ID) private platformId: Object) {}

  get(key: string) {
    if (isPlatformBrowser(this.platformId)) {
      return sessionStorage.getItem(key);
    }
    return null; // Return null if not in browser environment
  }

  set(key: string, value: any) {
    if (isPlatformBrowser(this.platformId)) {
      sessionStorage.setItem(key, value);
    } else {
      this.cacheMap.set(key, value);
    }
  }

  remove(key: string) {
    if (isPlatformBrowser(this.platformId)) {
      sessionStorage.removeItem(key);
    } else {
      this.cacheMap.delete(key);
    }
  }

  clear() {
    if (isPlatformBrowser(this.platformId)) {
      sessionStorage.clear();
    } else {
      this.cacheMap.clear();
    }
  }
}

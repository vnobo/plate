import { isPlatformBrowser } from '@angular/common';
import { afterNextRender, Inject, Injectable, PLATFORM_ID } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class SessionStorageService {
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
      console.log('Browser session storage loaded');
      this.storage = sessionStorage;
    });
  }

  get(key: string) {
    if (isPlatformBrowser(this.platformId)) {
      return sessionStorage.getItem(key);
    }
    return null; // Return null if not in browser environment
  }

  set(key: string, value: string) {
    this.storage.setItem(key, value);
  }

  remove(key: string) {
    this.storage.removeItem(key);
  }

  clear() {
    this.storage.clear();
  }
}

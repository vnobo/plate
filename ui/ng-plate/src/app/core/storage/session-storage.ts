import { isPlatformBrowser } from '@angular/common';
import { Service, PLATFORM_ID, inject } from '@angular/core';

@Service()
export class SessionStorage {
  private readonly _platformId = inject(PLATFORM_ID);
  private readonly isBrowser = isPlatformBrowser(this._platformId);

  setItem(key: string, value: string): void {
    if (this.isBrowser) {
      sessionStorage.setItem(key, value);
    }
  }

  getItem(key: string): string | null {
    if (this.isBrowser) {
      return sessionStorage.getItem(key);
    }
    return null;
  }

  removeItem(key: string): void {
    if (this.isBrowser) {
      sessionStorage.removeItem(key);
    }
  }

  clear(): void {
    if (this.isBrowser) {
      sessionStorage.clear();
    }
  }
}

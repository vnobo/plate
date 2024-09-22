import { Inject, Injectable, InjectionToken } from '@angular/core';

export const BROWSER_STORAGE = new InjectionToken<Storage>('Browser Storage', {
  providedIn: 'root',
  factory: () => localStorage,
});

@Injectable({
  providedIn: 'root',
})
export class BrowserStorage {
  constructor(@Inject(BROWSER_STORAGE) public storage: Storage) {
  }

  get(key: string) {
    const itemStr = this.storage.getItem(key);
    if (itemStr) {
      return atob(itemStr);
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

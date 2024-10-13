import { Inject, Injectable, InjectionToken } from '@angular/core';

export const SESSION_STORAGE = new InjectionToken<Storage>('Session Storage', {
  providedIn: 'root',
  factory: () => sessionStorage,
});

@Injectable({
  providedIn: 'root',
})
export class SessionStorageService {
  constructor(@Inject(SESSION_STORAGE) public storage: Storage) {
  }

  get(key: string) {
    var value = this.storage.getItem(key);
    return value && atob(value);
  }

  set(key: string, value: string) {
    this.storage.setItem(key, btoa(value));
  }

  remove(key: string) {
    this.storage.removeItem(key);
  }

  clear() {
    this.storage.clear();
  }
}

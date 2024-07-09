import { Injectable } from '@angular/core';
import { SessionStorageService } from './session-storage.service';

@Injectable({
  providedIn: 'root',
})
export class SessionStorageServerService extends SessionStorageService {
  constructor() {
    super({
      clear: () => {
        console.log('clear');
      },
      getItem: (key: string) => {
        return null;
      },
      setItem: (key: string, value: string) => {
        console.log('setItem is key ', key, ' and value ', value);
      },
      key: (index: number) => {
        console.log('key is index ', index);
        return index.toString();
      },
      length: 0,
      removeItem: (key: string) => {
        console.log('removeItem is key ', key);
      },
    });
  }
}

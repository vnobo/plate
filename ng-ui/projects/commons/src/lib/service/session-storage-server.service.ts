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
      getItem: (key: string) => btoa(JSON.stringify({ key })),
      setItem: (key: string, value: string) => btoa(JSON.stringify({ [key]: value })),
      key: (index: number) => index.toString(),
      length: 0,
      removeItem: (key: string) => btoa(JSON.stringify({ key })),
    });
  }
}

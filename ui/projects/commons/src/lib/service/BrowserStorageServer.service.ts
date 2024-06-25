import {Injectable} from '@angular/core';
import {BrowserStorageService} from './BrowserStorage.service';

@Injectable({
  providedIn: 'root',
})
export class BrowserStorageServerService extends BrowserStorageService {
  constructor() {
    super({
      clear: () => {
        console.log('clear');
      },
      getItem: (key: string) => btoa(JSON.stringify({key})),
      setItem: (key: string, value: string) =>
        btoa(JSON.stringify({[key]: value})),
      key: (index: number) => index.toString(),
      length: 0,
      removeItem: (key: string) => btoa(JSON.stringify({key})),
    });
  }
}

import { Injectable } from '@angular/core';
import { NzMessageService } from 'ng-zorro-antd/message';

@Injectable({
  providedIn: 'root',
})
export class MessageService {
  options = {
    nzDuration: 5000,
    nzAnimate: true,
    nzPauseOnHover: true,
  };

  constructor(private _message: NzMessageService) {}

  success(message: string, duration?: number): void {
    this.options.nzDuration = duration ? duration : this.options.nzDuration;
    this._message.success(message, this.options);
  }

  error(message: string, duration?: number): void {
    this.options.nzDuration = duration ? duration : this.options.nzDuration;
    this._message.error(message, this.options);
  }

  warning(message: string, duration?: number): void {
    this.options.nzDuration = duration ? duration : this.options.nzDuration;
    this._message.warning(message, this.options);
  }
}

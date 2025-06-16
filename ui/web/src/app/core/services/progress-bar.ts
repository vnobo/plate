/**
 * 进度条服务类
 *
 * 用于控制全局进度条的显示和隐藏状态。
 * 提供响应式的状态管理，支持防抖和状态变更追踪。
 *
 * @example
 * ```typescript
 * constructor(private progressBar: ProgressBar) {}
 *
 * // 显示进度条
 * this.progressBar.show();
 *
 * // 隐藏进度条
 * this.progressBar.hide();
 *
 * // 订阅进度条状态
 * this.progressBar.isShow$.subscribe(isShow => {
 *   console.log('进度条状态:', isShow);
 * });
 * ```
 */
import {Injectable, signal} from '@angular/core';
import {toObservable} from '@angular/core/rxjs-interop';
import {debounceTime, distinctUntilChanged} from 'rxjs';

@Injectable({ providedIn: 'root' })
export class ProgressBar {
  private isShow = signal(false);

  isShow$ = toObservable(this.isShow).pipe(debounceTime(500), distinctUntilChanged());

  show(): void {
    this.isShow.set(true);
  }

  hide(): void {
    this.isShow.set(false);
  }
}

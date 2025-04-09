import {Injectable, signal} from '@angular/core';
import {toObservable} from '@angular/core/rxjs-interop';
import {debounceTime, distinctUntilChanged, Observable, tap} from 'rxjs';

@Injectable({ providedIn: 'root' })
export class ProgressBar {
  private isShow = signal(false);

  isShow$: Observable<boolean> = toObservable(this.isShow).pipe(
    debounceTime(100),
    distinctUntilChanged(),
    tap(res => console.log(`Progress bar show is: ${res}`)),
  );

  show(): void {
    this.isShow.set(true);
  }

  hide(): void {
    this.isShow.set(false);
  }
}

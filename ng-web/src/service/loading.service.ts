import { Injectable, signal } from '@angular/core';
import { toObservable } from '@angular/core/rxjs-interop';
import { debounceTime, distinctUntilChanged, Observable, tap } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class LoadingService {
  private progressSource = signal(false);
  progress$: Observable<boolean> = toObservable(this.progressSource).pipe(
    debounceTime(100),
    distinctUntilChanged(),
    tap(res => console.log(`Loading Spin show is: ${res}`)),
  );

  show(): void {
    this.progressSource.set(true);
  }

  hide(): void {
    this.progressSource.set(false);
  }
}

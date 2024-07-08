import { Injectable } from '@angular/core';
import { debounceTime, distinctUntilChanged, Observable, Subject, tap } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class LoadingService {
  private progressSource: Subject<boolean> = new Subject<boolean>();
  progress$: Observable<boolean> = this.progressSource.asObservable().pipe(
    debounceTime(100),
    distinctUntilChanged(),
    tap(res => console.log(`Loading Spin show is: ${res}`))
  );

  show(): void {
    this.progressSource.next(true);
  }

  hide(): void {
    this.progressSource.next(false);
  }
}

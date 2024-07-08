import { Injectable } from '@angular/core';
import { NavigationEnd, NavigationStart, Router } from '@angular/router';
import { debounceTime, distinctUntilChanged, Observable, Subject, tap } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class LoadingService {
  private progressSource: Subject<boolean> = new Subject<boolean>();
  progress$: Observable<boolean> = this.progressSource.asObservable().pipe(
    debounceTime(500),
    distinctUntilChanged(),
    tap(res => console.log(`Loading Spin show is: ${res}`))
  );
  constructor(public router: Router) {
    this.progressSource.next(true);
    router.events.subscribe(event => {
      if (event instanceof NavigationStart) {
        this.show();
      }
      if (event instanceof NavigationEnd) {
        this.hide();
      }
    });
  }
  show(): void {
    this.progressSource.next(true);
  }

  hide(): void {
    this.progressSource.next(false);
  }
}

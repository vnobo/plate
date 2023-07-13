import {Injectable} from '@angular/core';
import {Observable, Subject} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class ProgressBarService {

  // Observable string sources
  private progressSource: Subject<boolean> = new Subject<boolean>();
  progress$: Observable<boolean> = this.progressSource.asObservable();

  show(): void {
    this.progressSource.next(true);
  }

  hide(): void {
    this.progressSource.next(false);
  }
}

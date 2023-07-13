import {Injectable} from '@angular/core';
import {Observable, Subject} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class LayoutService {

  private layoutHideSource: Subject<boolean> = new Subject<boolean>();
  layoutHide$: Observable<boolean> = this.layoutHideSource.asObservable();

  isHide(isHide: boolean): void {
    this.layoutHideSource.next(isHide);
  }
}

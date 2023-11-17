import {Component} from '@angular/core';
import {AsyncPipe, CommonModule} from '@angular/common';
import {RouterOutlet} from '@angular/router';
import {NzBackTopModule} from "ng-zorro-antd/back-top";
import {NzSpinModule} from "ng-zorro-antd/spin";
import {debounceTime, distinctUntilChanged, Observable, tap} from "rxjs";
import {LoadingService} from "./core/loading.service";

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, AsyncPipe, NzBackTopModule, NzSpinModule, RouterOutlet],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent {
  loadingShow$: Observable<boolean> | undefined;

  constructor(private loading: LoadingService) {
  }

  ngOnInit(): void {
    this.loadingShow$ = this.loading.progress$.pipe(
      debounceTime(500),
      distinctUntilChanged(),
      tap((res) => console.log(`Loading show is: ${res}`))
    );
  }
}

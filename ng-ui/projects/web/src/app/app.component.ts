import {AsyncPipe} from '@angular/common';
import {Component} from '@angular/core';
import {RouterOutlet} from '@angular/router';
import {toSignal} from '@angular/core/rxjs-interop';

import {NzBackTopModule} from 'ng-zorro-antd/back-top';
import {NzSpinModule} from 'ng-zorro-antd/spin';
import {debounceTime, distinctUntilChanged, tap} from 'rxjs';
import {LoadingService} from '../core/loading.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, AsyncPipe, NzBackTopModule, NzSpinModule],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss',
})
export class AppComponent {
  loadingShow = toSignal(
    this.loading.progress$.pipe(
      debounceTime(500),
      distinctUntilChanged(),
      tap(res => console.log(`Loading show is: ${res}`))
    ),
    { initialValue: false }
  );

  constructor(private loading: LoadingService) {}

  ngOnInit(): void {
    this.loading.show();
  }
}

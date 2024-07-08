import { AsyncPipe, NgIf } from '@angular/common';
import { Component, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { NavigationEnd, NavigationStart, Router, RouterOutlet } from '@angular/router';
import { MatProgressBarModule } from '@angular/material/progress-bar';

import { NzBackTopModule } from 'ng-zorro-antd/back-top';
import { NzSpinModule } from 'ng-zorro-antd/spin';
import { LoadingService } from '../core/loading.service';
import { debounceTime, distinctUntilChanged } from 'rxjs';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, AsyncPipe, NzBackTopModule, NzSpinModule, MatProgressBarModule, NgIf],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss',
})
export class AppComponent {
  loadingShow = toSignal(this.loading.progress$, { initialValue: false });
  progressShow = signal(true);
  constructor(private loading: LoadingService, private router: Router) {
    router.events.pipe(debounceTime(100), distinctUntilChanged()).subscribe(event => {
      if (event instanceof NavigationStart) {
        this.progressShow.set(true);
      }
      if (event instanceof NavigationEnd) {
        this.progressShow.set(false);
      }
    });
  }
}

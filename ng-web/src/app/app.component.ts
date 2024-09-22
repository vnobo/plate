import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { NavigationEnd, NavigationStart, Router, RouterLink, RouterOutlet } from '@angular/router';
import { NzLayoutModule } from 'ng-zorro-antd/layout';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { NzSpinModule } from 'ng-zorro-antd/spin';
import { NzBackTopModule } from 'ng-zorro-antd/back-top';
import { toSignal } from '@angular/core/rxjs-interop';
import { ProgressBar } from './core/progress-bar';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    RouterOutlet,
    NzLayoutModule,
    NzSpinModule,
    NzBackTopModule,
    MatProgressBarModule,
  ],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss'],
})
export class AppComponent implements OnInit {
  progressShow;

  constructor(private loading: ProgressBar, private router: Router) {
    this.progressShow = toSignal(this.loading.progress$, { initialValue: false });
  }

  ngOnInit(): void {
    this.router.events.pipe(debounceTime(100), distinctUntilChanged()).subscribe(event => {
      if (event instanceof NavigationStart) {
        this.progressShow.apply(() => true);
      }
      if (event instanceof NavigationEnd) {
        this.progressShow.apply(() => false);
      }
    });
  }
}

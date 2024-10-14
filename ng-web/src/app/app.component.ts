import { CommonModule } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import { NavigationEnd, NavigationStart, Router, RouterLink, RouterOutlet } from '@angular/router';
import { NzLayoutModule } from 'ng-zorro-antd/layout';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { NzSpinModule } from 'ng-zorro-antd/spin';
import { NzBackTopModule } from 'ng-zorro-antd/back-top';
import { toSignal } from '@angular/core/rxjs-interop';
import { ProgressBar } from './core/progress-bar';

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
  progressBar = inject(ProgressBar);
  progressShow = toSignal(this.progressBar.progress$, { initialValue: false });

  constructor(private router: Router) {
  }

  ngOnInit(): void {
    this.router.events.subscribe(event => {
      if (event instanceof NavigationStart) {
        this.progressShow.apply(() => true);
      }
      if (event instanceof NavigationEnd) {
        this.progressShow.apply(() => false);
      }
    });
  }
}

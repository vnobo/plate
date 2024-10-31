import { Component, ElementRef, inject, OnInit, Renderer2 } from '@angular/core';
import { NavigationEnd, NavigationError, NavigationStart, Router, RouterOutlet } from '@angular/router';
import { ProgressBar } from './core/progress-bar';
import { VERSION as VERSION_ZORRO } from 'ng-zorro-antd/version';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet],
  template: '<router-outlet></router-outlet>',
  styleUrls: ['./app.component.scss'],
})
export class AppComponent implements OnInit {
  private readonly progressBar = inject(ProgressBar);
  private readonly router = inject(Router);

  constructor(el: ElementRef, renderer: Renderer2) {
    renderer.setAttribute(el.nativeElement, 'ng-zorro-version', VERSION_ZORRO.full);
  }

  ngOnInit(): void {
    let configLoad = false;
    this.router.events.subscribe(event => {
      if (event instanceof NavigationStart) {
        configLoad = true;
      }
      if (configLoad && event instanceof NavigationError) {
      }
      if (event instanceof NavigationEnd) {
      }
    });
  }
}

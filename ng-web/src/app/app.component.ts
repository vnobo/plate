import { Component, ElementRef, inject, OnInit, Renderer2, signal } from '@angular/core';
import { NavigationEnd, NavigationError, NavigationStart, Router, RouterOutlet } from '@angular/router';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { VERSION as VERSION_ZORRO } from 'ng-zorro-antd/version';
import { NgIf } from '@angular/common';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, MatProgressBarModule, NgIf],
  template: `<div class="fixed-top">
      @if(progressShow()){
      <mat-progress-bar mode="query"></mat-progress-bar>
      }
    </div>
    <router-outlet></router-outlet>`,
})
export class AppComponent implements OnInit {
  private readonly router = inject(Router);

  progressShow = signal(false);

  constructor(el: ElementRef, renderer: Renderer2) {
    renderer.setAttribute(el.nativeElement, 'ng-zorro-version', VERSION_ZORRO.full);
  }

  ngOnInit(): void {
    let configLoad = false;
    this.router.events.subscribe(event => {
      if (event instanceof NavigationStart) {
        configLoad = true;
        this.progressShow.set(true);
      }
      if (configLoad && event instanceof NavigationError) {
      }
      if (event instanceof NavigationEnd) {
        this.progressShow.set(false);
      }
    });
  }
}

import { NgIf } from '@angular/common';
import { Component, ElementRef, inject, OnInit, Renderer2, signal } from '@angular/core';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { NavigationEnd, NavigationError, NavigationStart, Router, RouterOutlet } from '@angular/router';
import { VERSION as VERSION_ZORRO } from 'ng-zorro-antd/version';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, MatProgressBarModule, NgIf],
  template: `
    <div class="fixed-top">
      <mat-progress-bar *ngIf="progressShow()" mode="query"></mat-progress-bar>
    </div>
    <router-outlet></router-outlet>
  `,
  styles: [
    `
      :host {
        min-height: 100%;
        min-width: 100%;
      }
    `,
  ],
})
export class AppComponent implements OnInit {
  progressShow = signal(false);
  private readonly router = inject(Router);

  constructor(el: ElementRef, renderer: Renderer2) {
    renderer.setAttribute(el.nativeElement, 'ng-zorro-version', VERSION_ZORRO.full);
    renderer.setAttribute(el.nativeElement.parentElement, 'data-bs-theme', 'dark');
  }

  ngOnInit(): void {
    let configLoad = false;
    this.router.events.subscribe(event => {
      if (event instanceof NavigationStart) {
        configLoad = true;
        this.progressShow.set(true);
      }
      if (configLoad && event instanceof NavigationError) {
        console.error(event.error);
      }
      if (event instanceof NavigationEnd) {
        configLoad = false;
        this.progressShow.set(false);
      }
    });
  }
}

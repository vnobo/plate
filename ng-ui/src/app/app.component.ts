import { Component, ElementRef, inject, OnInit, Renderer2, signal } from '@angular/core';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { NavigationEnd, NavigationError, NavigationStart, Router, RouterOutlet } from '@angular/router';
import { NzFloatButtonModule } from 'ng-zorro-antd/float-button';
import { VERSION as VERSION_ZORRO } from 'ng-zorro-antd/version';
import { debounceTime, delay } from 'rxjs';

/**
 * The root component of the application.
 *
 * @selector app-root
 * @imports RouterOutlet, MatProgressBarModule, NgIf
 * @template
 *   Contains a fixed-top progress bar that is displayed during navigation events
 *   and a router outlet for displaying routed components.
 * @styles
 *   Ensures the host element takes up the full height and width of the viewport.
 */
@Component({
  selector: 'app-root',
  imports: [RouterOutlet, MatProgressBarModule, NzFloatButtonModule],
  template: `
    @if (progressShow()) {
    <div class="fixed-top">
      <mat-progress-bar mode="query"></mat-progress-bar>
    </div>
    }
    <router-outlet></router-outlet>
    <nz-float-button-top [nzVisibilityHeight]="100"></nz-float-button-top>
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
  /**
   * Signal to control the visibility of the progress bar.
   */
  progressShow = signal(true);
  /**
   * Router instance for subscribing to navigation events.
   */
  private readonly router = inject(Router);

  /**
   * Constructs the AppComponent and sets initial attributes on the host element.
   *
   * @param el - The element reference of the host element.
   * @param renderer - The renderer for manipulating the DOM.
   */
  constructor(el: ElementRef, renderer: Renderer2) {
    renderer.setAttribute(el.nativeElement, 'ng-zorro-version', VERSION_ZORRO.full);
    //renderer.setAttribute(el.nativeElement.parentElement, 'data-bs-theme', 'dark');
  }

  /**
   * Initializes the component and subscribes to router events to control the progress bar visibility.
   */
  ngOnInit(): void {}
}

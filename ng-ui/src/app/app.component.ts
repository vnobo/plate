import {CommonModule} from '@angular/common';
import {Component, ElementRef, inject, OnInit, Renderer2} from '@angular/core';
import {MatProgressBarModule} from '@angular/material/progress-bar';
import {RouterOutlet} from '@angular/router';
import {NzFloatButtonModule} from 'ng-zorro-antd/float-button';
import {VERSION as VERSION_ZORRO} from 'ng-zorro-antd/version';
import {ProgressBar} from './core/services/progress-bar';

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
  imports: [RouterOutlet, MatProgressBarModule, NzFloatButtonModule, CommonModule],
  template: `
    @if (progressBar.isShow$ | async) {
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
  progressBar = inject(ProgressBar);
  constructor(el: ElementRef, renderer: Renderer2) {
    renderer.setAttribute(el.nativeElement, 'ng-zorro-version', VERSION_ZORRO.full);
  }

  ngOnInit(): void {}
}

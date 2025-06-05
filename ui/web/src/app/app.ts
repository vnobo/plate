import { Component, ElementRef, inject, Renderer2 } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { ProgressBar } from './core/services/progress-bar';
import { CommonModule } from '@angular/common';
import { MatProgressBarModule } from '@angular/material/progress-bar';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, CommonModule, MatProgressBarModule],
  template: ` @if (progressBar.isShow$ | async) {
    <div class="fixed-top">
      <mat-progress-bar mode="query"></mat-progress-bar>
    </div>
    }
    <router-outlet />`,
  styles: [
    `
      :host {
        min-height: 100%;
        min-width: 100%;
      }
    `,
  ],
})
export class App {
  progressBar = inject(ProgressBar);
  constructor(el: ElementRef, renderer: Renderer2) {
    this.progressBar.show();
  }
}

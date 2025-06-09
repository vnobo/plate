import {
  afterEveryRender,
  afterNextRender,
  Component,
  ElementRef,
  inject,
  OnInit,
  Renderer2,
} from '@angular/core';
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
        min-height: 100vh;
        min-width: 100%;
      }
    `,
  ],
})
export class App implements OnInit {
  progressBar = inject(ProgressBar);

  constructor(el: ElementRef, renderer: Renderer2) {
    afterEveryRender(() => {
      // get html element
      const targetElement = el.nativeElement.parentElement.parentElement;
      const attributes = [
        { name: 'data-bs-theme-base', value: 'slate' },
        { name: 'data-bs-theme', value: 'dark' },
        { name: 'data-bs-theme-radius', value: '1' },
        { name: 'data-bs-theme-primary', value: 'orange' },
      ];

      attributes.forEach(attr => {
        renderer.setAttribute(targetElement, attr.name, attr.value);
      });
    });
  }

  ngOnInit(): void {
    this.progressBar.show();
  }
}

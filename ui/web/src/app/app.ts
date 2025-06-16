import { CommonModule } from '@angular/common';
import {
  afterNextRender,
  Component,
  ElementRef,
  inject,
  OnInit,
  Renderer2,
  signal,
} from '@angular/core';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { RouterOutlet } from '@angular/router';
import { ProgressBar } from './core/services/progress-bar';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, CommonModule, MatProgressBarModule],
  template: ` @if (progress()) {
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
export class App implements OnInit {
  private readonly progressBar = inject(ProgressBar);

  progress = signal(false);

  constructor(el: ElementRef, renderer: Renderer2) {
    afterNextRender(() => {
      // get html element
      const targetElement = el.nativeElement.parentElement.parentElement;
      const attributes = [
        { name: 'data-bs-theme-base', value: 'slate' },
        { name: 'data-bs-theme', value: 'light' },
        { name: 'data-bs-theme-radius', value: '1' },
        { name: 'data-bs-theme-primary', value: 'teal' },
      ];

      attributes.forEach(attr => {
        renderer.setAttribute(targetElement, attr.name, attr.value);
      });
    });
  }

  ngOnInit(): void {
    this.progressBar.isShow$.subscribe(isShow => {
      this.progress.set(isShow);
    });
  }
}

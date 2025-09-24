/**
 * 应用程序根组件
 *
 * 负责应用程序的全局布局和主题设置，包括：
 * - 全局进度条的显示与隐藏
 * - 应用主题属性的设置
 * - 路由出口的渲染
 */
import { CommonModule } from '@angular/common';
import {
  afterNextRender,
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  inject,
  OnDestroy,
  OnInit,
  Renderer2,
  signal,
} from '@angular/core';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { RouterOutlet } from '@angular/router';
import { ProgressBar } from './core/services/progress-bar';
import { Subscription } from 'rxjs';

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
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class App implements OnInit, OnDestroy {
  private readonly progressBar = inject(ProgressBar);
  private subscription: Subscription | null = null;

  progress = signal(false);

  constructor(private el: ElementRef, private renderer: Renderer2) {
    afterNextRender(() => {
      // 安全地获取目标元素，避免空引用异常
      const nativeElement = el.nativeElement;
      if (
        !nativeElement ||
        !nativeElement.parentElement ||
        !nativeElement.parentElement.parentElement
      ) {
        console.warn('Unable to find target element for theme attributes');
        return;
      }

      const targetElement = nativeElement.parentElement.parentElement;
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
    this.subscription = this.progressBar.isShow$.subscribe(isShow => {
      this.progress.set(isShow);
    });
  }

  ngOnDestroy(): void {
    if (this.subscription) {
      this.subscription.unsubscribe();
      this.subscription = null;
    }
  }
}

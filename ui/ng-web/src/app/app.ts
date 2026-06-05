import { Component, signal, afterNextRender, inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { DatePipe, DecimalPipe, CurrencyPipe } from '@angular/common';
import { RouterOutlet } from '@angular/router';
import dayjs from './core/dayjs.locale';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, DatePipe, DecimalPipe, CurrencyPipe],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App {
  private readonly platformId = inject(PLATFORM_ID);

  protected readonly title = signal('ng-web');
  protected readonly btnClicked = signal(false);

  constructor() {
    // 应用就绪后隐藏加载动画
    afterNextRender(() => {
      if (isPlatformBrowser(this.platformId)) {
        const loading = document.getElementById('app-loading');
        if (loading) {
          loading.classList.add('hidden');
          // 过渡动画结束后移除 DOM
          loading.addEventListener('transitionend', () => loading.remove(), { once: true });
        }
      }
    });
  }

  protected toggleBtn(): void {
    this.btnClicked.update((v) => !v);
  }

  // i18n test data
  protected readonly currentDate = new Date();
  protected readonly dayjsDate = dayjs();
  protected readonly sampleNumber = 1234567.89;
  protected readonly samplePrice = 99.99;
}

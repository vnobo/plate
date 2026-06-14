import { Component, computed, Injectable, input, signal } from '@angular/core';
import { toObservable } from '@angular/core/rxjs-interop';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';

@Component({
  selector: 'tabler-progress',
  template: `
    <div
      class="progress-container"
      [style.height]="height()"
      [style.background-color]="backgroundColor()"
      [style.border-radius.px]="2"
      [style.overflow]="'hidden'"
      [style.position]="'relative'"
    >
      <div
        class="progress-bar"
        [style.width]="progressWidth()"
        [style.background-color]="color()"
        [style.height]="'100%'"
        [style.transition]="'width 0.3s ease'"
        [style.border-radius.px]="2"
      ></div>
    </div>
  `,
  styles: `
    .progress-container {
      position: relative;
      width: 100%;
    }

    .progress-bar {
      display: flex;
      align-items: center;
      justify-content: center;
      color: white;
      font-size: 10px;
      font-weight: bold;
      text-shadow: 0 1px 1px rgba(0, 0, 0, 0.5);
    }
  `,
})
export class Progress {
  value = input<number>(0);
  max = input<number>(100);
  height = input<string>('4px');
  color = input<string>('#07bff');
  backgroundColor = input<string>('#e0e0e0');

  protected readonly progressWidth = computed(() => {
    const current = this.value();
    const total = this.max();
    const percentage = Math.min(100, Math.max(0, (current / total) * 100));
    return `${percentage}%`;
  });
}

@Injectable({ providedIn: 'root' })
export class ProgressService {
  private readonly isShow = signal(false);

  readonly isShow$ = toObservable(this.isShow).pipe(debounceTime(500), distinctUntilChanged());

  show(): void {
    this.isShow.set(true);
  }

  hide(): void {
    this.isShow.set(false);
  }

  toggle(): void {
    this.isShow.update((show) => !show);
  }

  isVisible(): boolean {
    return this.isShow();
  }
}

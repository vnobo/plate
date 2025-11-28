import { Component, input, computed, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-progress-bar',
  standalone: true,
  imports: [CommonModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div
      class="progress-container"
      role="progressbar"
      [attr.aria-valuenow]="progress()"
      aria-valuemin="0"
      aria-valuemax="100"
    >
      <div class="progress-bar" [style.width.%]="progress()">
        <span class="progress-text">{{ progress() }}%</span>
      </div>
    </div>
  `,
  styles: `
    .progress-container {
      width: 100%;
      height: 4px;
      background-color: #f0f0f0;
      position: fixed;
      top: 0;
      left: 0;
      z-index: 1000;
      overflow: hidden;
    }

    .progress-bar {
      height: 100%;
      background: linear-gradient(90deg, #007bff 0%, #0056b3 100%);
      transition: width 0.3s ease;
      position: relative;
    }

    .progress-text {
      position: absolute;
      right: 10px;
      top: 50%;
      transform: translateY(-50%);
      font-size: 10px;
      color: white;
      font-weight: bold;
    }
  `,
})
export class ProgressBarComponent {
  progress = input<number>(0);
}

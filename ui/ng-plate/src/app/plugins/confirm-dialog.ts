import { Component, input } from '@angular/core';

/** Tabler-styled confirmation body; resolves via the `onResult` callback. */
@Component({
  selector: 'app-confirm-dialog',
  template: `
    <div class="text-center py-2">
      <p class="text-secondary mb-3">{{ message() }}</p>
      <div class="btn-list justify-content-center">
        <button type="button" class="btn btn-outline-secondary" (click)="close(false)">
          {{ cancelText() }}
        </button>
        <button
          type="button"
          class="btn"
          [class.btn-danger]="danger()"
          [class.btn-primary]="!danger()"
          (click)="close(true)"
        >
          {{ confirmText() }}
        </button>
      </div>
    </div>
  `,
})
export class ConfirmDialogComponent {
  message = input.required<string>();
  confirmText = input('确定');
  cancelText = input('取消');
  danger = input(false);
  /** Resolves the promise returned by `ModalsService.confirm`. */
  onResult = input<(ok: boolean) => void>();

  close(ok: boolean): void {
    this.onResult()?.(ok);
    if (typeof document === 'undefined') {
      return;
    }
    const el = document.getElementById('exampleModal');
    tabler?.Modal?.getOrCreateInstance(el)?.hide();
  }
}

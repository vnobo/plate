import { CommonModule } from '@angular/common';
import {
  Component,
  input,
  output,
  model,
  signal,
  computed,
  ChangeDetectionStrategy,
} from '@angular/core';

export interface TransferItem {
  key: string;
  title: string;
  description?: string;
  disabled?: boolean;
}

export interface TransferData {
  source: TransferItem[];
  target: TransferItem[];
}

@Component({
  selector: 'tabler-transfer',
  imports: [CommonModule],
  template: `
    <div class="row row-deck row-cards">
      <div class="col-5">
        <div class="card">
          <div class="card-header">
            <label class="form-check m-0">
              <input
                id="lerfTitleCheckbox"
                class="form-check-input"
                type="checkbox"
                [checked]="sourceChecked()"
                (change)="toggleAllSource($any($event.target).checked)"
                [disabled]="sourceData().length === 0 || disabled()"
                aria-label="Checkbox for following text input"
              />
              <span class="form-check-label" for="lerfTitleCheckbox">
                {{ leftTitle() || 'Source' }} ({{ sourceData().length }})
              </span>
            </label>
          </div>
          <div class="card-body">
            <ol class="list-group list-group-flush">
              @for (item of sourceData(); track item.key) {
              <div class="list-group-item p-2 m-0">
                <label
                  class="form-check m-0"
                  [class.transfer-item-disabled]="item.disabled || disabled()"
                >
                  <input
                    class="form-check-input"
                    type="checkbox"
                    [checked]="itemChecked(item.key)"
                    (change)="toggleItem(item.key, $any($event.target).checked)"
                    [disabled]="item.disabled || disabled()"
                  />
                  <span class="form-check-label">
                    {{ item.title }}
                    @if (item.description) {
                    {{ item.description }}
                    }
                  </span>
                </label>
              </div>
              }
            </ol>
          </div>
        </div>
      </div>
      <div class="col-2">
        <div
          class="p-3 w-full d-flex flex-fill flex-wrap gap-2 justify-content-center justify-content-center"
        >
          <div class="btn-group-vertical" role="group">
            <button
              type="button"
              class="btn btn-outline-secondary btn-sm"
              [disabled]="!hasUnselectedSourceItems() || disabled()"
              (click)="moveToTarget()"
              [title]="titles()[0] || 'Move to right'"
            >
              <svg
                class="icon icon-tabler icons-tabler-outline icon-tabler-chevron-right"
                width="16"
                height="16"
                viewBox="0 0 24 24"
                stroke-width="2"
                stroke="currentColor"
                fill="none"
                stroke-linecap="round"
                stroke-linejoin="round"
              >
                <path stroke="none" d="M0 0h24v24H0z" fill="none"></path>
                <path d="M9 6l6 6l-6 6"></path>
              </svg>
            </button>
            <button
              type="button"
              class="btn btn-outline-secondary btn-sm mt-2"
              [disabled]="!hasTargetItems() || disabled()"
              (click)="moveToSource()"
              [title]="titles()[1] || 'Move to left'"
            >
              <svg
                class="icon icon-tabler icons-tabler-outline icon-tabler-chevron-left"
                width="16"
                height="16"
                viewBox="0 0 24 24"
                stroke-width="2"
                stroke="currentColor"
                fill="none"
                stroke-linecap="round"
                stroke-linejoin="round"
              >
                <path stroke="none" d="M0 0h24v24H0z" fill="none"></path>
                <path d="M15 6l-6 6l6 6"></path>
              </svg>
            </button>
          </div>
        </div>
      </div>
      <div class="col-5">
        <div class="card">
          <div class="card-header">
            <label class="form-check m-0">
              <input
                class="form-check-input"
                type="checkbox"
                [checked]="targetChecked()"
                (change)="toggleAllTarget($any($event.target).checked)"
                [disabled]="targetData().length === 0 || disabled()"
              />
              <span class="form-check-label">
                {{ rightTitle() || 'Target' }} ({{ targetData().length }})
              </span>
            </label>
          </div>
          <div class="card-body">
            <ol class="list-group list-group-flush">
              @for (item of targetData(); track item.key) {
              <div class="list-group-item p-2 m-0">
                <label
                  class="form-check m-0"
                  [class.transfer-item-disabled]="item.disabled || disabled()"
                >
                  <input
                    class="form-check-input"
                    type="checkbox"
                    [checked]="itemChecked(item.key)"
                    (change)="toggleItem(item.key, $any($event.target).checked)"
                    [disabled]="item.disabled || disabled()"
                  />
                  <div class="form-check-label">
                    {{ item.title }}
                    @if (item.description) {
                    {{ item.description }}
                    }
                  </div>
                </label>
              </div>
              }
            </ol>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: `
  `,
})
export class TransferComponent {
  // Inputs
  data = input.required<TransferItem[]>();
  modelValue = model<string[]>([]);
  titles = input<[string, string]>(['Move to right', 'Move to left']);
  leftTitle = input<string>();
  rightTitle = input<string>();
  disabled = input<boolean>(false);

  // Outputs
  modelValueChange = output<string[]>();
  change = output<TransferData>();

  // Internal state
  private selectedKeys = signal<string[]>([]);

  // Computed properties
  sourceData = computed(() => {
    const currentData = this.data();
    const selected = this.modelValue();
    return currentData.filter((item) => !selected.includes(item.key));
  });

  targetData = computed(() => {
    const currentData = this.data();
    const selected = this.modelValue();
    return currentData.filter((item) => selected.includes(item.key));
  });

  sourceChecked = computed(() => {
    const source = this.sourceData();
    if (source.length === 0) return false;

    const selectedSourceKeys = source
      .filter((item) => this.itemChecked(item.key))
      .map((item) => item.key);

    return selectedSourceKeys.length > 0 && selectedSourceKeys.length === source.length;
  });

  targetChecked = computed(() => {
    const target = this.targetData();
    if (target.length === 0) return false;

    const selectedTargetKeys = target
      .filter((item) => this.itemChecked(item.key))
      .map((item) => item.key);

    return selectedTargetKeys.length > 0 && selectedTargetKeys.length === target.length;
  });

  hasUnselectedSourceItems = computed(() => {
    return this.sourceData().some((item) => !this.selectedKeys().includes(item.key));
  });

  hasTargetItems = computed(() => {
    return this.targetData().length > 0;
  });

  // Helper methods
  itemChecked(key: string): boolean {
    return this.selectedKeys().includes(key);
  }

  toggleItem(key: string, checked: boolean) {
    if (checked) {
      this.selectedKeys.update((keys) => [...keys, key]);
    } else {
      this.selectedKeys.update((keys) => keys.filter((k) => k !== key));
    }
  }

  toggleAllSource(checked: boolean) {
    if (checked) {
      const sourceKeys = this.sourceData()
        .filter((item) => !item.disabled)
        .map((item) => item.key);
      this.selectedKeys.set([...new Set([...this.selectedKeys(), ...sourceKeys])]);
    } else {
      const sourceKeys = this.sourceData().map((item) => item.key);
      this.selectedKeys.update((keys) => keys.filter((key) => !sourceKeys.includes(key)));
    }
  }

  toggleAllTarget(checked: boolean) {
    if (checked) {
      const targetKeys = this.targetData()
        .filter((item) => !item.disabled)
        .map((item) => item.key);
      this.selectedKeys.set([...new Set([...this.selectedKeys(), ...targetKeys])]);
    } else {
      const targetKeys = this.targetData().map((item) => item.key);
      this.selectedKeys.update((keys) => keys.filter((key) => !targetKeys.includes(key)));
    }
  }

  moveToTarget() {
    const keysToMove = this.selectedKeys().filter((key) =>
      this.sourceData().some((item) => item.key === key)
    );

    if (keysToMove.length > 0) {
      const newSelected = [...this.modelValue(), ...keysToMove];
      this.modelValue.set(newSelected);
      this.selectedKeys.update((keys) => keys.filter((key) => !keysToMove.includes(key)));
      this.emitChange();
    }
  }

  moveToSource() {
    const keysToMove = this.selectedKeys().filter((key) =>
      this.targetData().some((item) => item.key === key)
    );

    if (keysToMove.length > 0) {
      const newSelected = this.modelValue().filter((key) => !keysToMove.includes(key));
      this.modelValue.set(newSelected);
      this.selectedKeys.update((keys) => keys.filter((key) => !keysToMove.includes(key)));
      this.emitChange();
    }
  }

  private emitChange() {
    this.modelValueChange.emit(this.modelValue());
    this.change.emit({
      source: this.sourceData(),
      target: this.targetData(),
    });
  }
}

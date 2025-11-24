
import { Component, computed, input, model, output, signal } from '@angular/core';

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
  imports: [],
  template: `
    <div class="transfer-container">
      <div class="transfer-panel">
        <div class="transfer-header">
          <label class="form-check mb-0">
            <input
              id="leftTitleCheckbox"
              class="form-check-input m-0"
              type="checkbox"
              [checked]="sourceChecked()"
              (change)="toggleAllSource($any($event.target).checked)"
              [disabled]="sourceData().length === 0 || disabled()"
              aria-label="Checkbox for following text input"
            />
            <span class="form-check-label ms-2">
              {{ leftTitle() || 'Source' }} ({{ sourceData().length }})
            </span>
          </label>
        </div>
        <div class="transfer-body">
          <div class="transfer-list">
            @for (item of sourceData(); track item.key) {
            <div
              class="transfer-list-item"
              [class.transfer-item-disabled]="item.disabled || disabled()"
            >
              <label class="form-check m-0 w-100">
                <input
                  class="form-check-input m-0"
                  type="checkbox"
                  [checked]="itemChecked(item.key)"
                  (change)="toggleItem(item.key, $any($event.target).checked)"
                  [disabled]="item.disabled || disabled()"
                />
                <div class="form-check-label ms-2 flex-fill">
                  <div class="transfer-item-title">{{ item.title }}</div>
                  @if (item.description) {
                  <div class="transfer-item-desc text-secondary">{{ item.description }}</div>
                  }
                </div>
              </label>
            </div>
            }
          </div>
        </div>
      </div>

      <div class="transfer-operation">
        <button
          type="button"
          class="btn btn-icon mb-2"
          [class.btn-disabled]="!hasUnselectedSourceItems() || disabled()"
          [disabled]="!hasUnselectedSourceItems() || disabled()"
          (click)="moveToTarget()"
          [title]="titles()[0] || 'Move to right'"
        >
          <svg
            class="icon icon-tabler icons-tabler-outline icon-tabler-chevron-right"
            width="20"
            height="20"
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
          class="btn btn-icon"
          [class.btn-disabled]="!hasTargetItems() || disabled()"
          [disabled]="!hasTargetItems() || disabled()"
          (click)="moveToSource()"
          [title]="titles()[1] || 'Move to left'"
        >
          <svg
            class="icon icon-tabler icons-tabler-outline icon-tabler-chevron-left"
            width="20"
            height="20"
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

      <div class="transfer-panel">
        <div class="transfer-header">
          <label class="form-check mb-0">
            <input
              class="form-check-input m-0"
              type="checkbox"
              [checked]="targetChecked()"
              (change)="toggleAllTarget($any($event.target).checked)"
              [disabled]="targetData().length === 0 || disabled()"
            />
            <span class="form-check-label ms-2">
              {{ rightTitle() || 'Target' }} ({{ targetData().length }})
            </span>
          </label>
        </div>
        <div class="transfer-body">
          <div class="transfer-list">
            @for (item of targetData(); track item.key) {
            <div
              class="transfer-list-item"
              [class.transfer-item-disabled]="item.disabled || disabled()"
            >
              <label class="form-check m-0 w-100">
                <input
                  class="form-check-input m-0"
                  type="checkbox"
                  [checked]="itemChecked(item.key)"
                  (change)="toggleItem(item.key, $any($event.target).checked)"
                  [disabled]="item.disabled || disabled()"
                />
                <div class="form-check-label ms-2 flex-fill">
                  <div class="transfer-item-title">{{ item.title }}</div>
                  @if (item.description) {
                  <div class="transfer-item-desc text-secondary">{{ item.description }}</div>
                  }
                </div>
              </label>
            </div>
            }
          </div>
        </div>
      </div>
    </div>
  `,
  styles: `
    .transfer-container {
      display: flex;
      gap: 1.5rem;
      align-items: stretch;
    }
    
    .transfer-panel {
      flex: 1;
      display: flex;
      flex-direction: column;
      border: 1px solid var(--tblr-border-color);
      border-radius: var(--tblr-border-radius);
      background: var(--tblr-bg-surface);
      box-shadow: var(--tblr-shadow-card);
    }
    
    .transfer-header {
      padding: 0.75rem 1rem;
      border-bottom: 1px solid var(--tblr-border-color);
      display: flex;
      align-items: center;
      background: var(--tblr-bg-surface-secondary);
      border-top-left-radius: calc(var(--tblr-border-radius) - 1px);
      border-top-right-radius: calc(var(--tblr-border-radius) - 1px);
    }
    
    .transfer-body {
      flex: 1;
      overflow: auto;
      max-height: 300px;
      padding: 0.5rem;
    }
    
    .transfer-list {
      display: flex;
      flex-direction: column;
      gap: 0.25rem;
    }
    
    .transfer-list-item {
      display: flex;
      align-items: center;
      padding: 0.5rem;
      border-radius: var(--tblr-border-radius);
      transition: all 0.15s ease;
    }
    
    .transfer-list-item:hover {
      background-color: var(--tblr-bg-surface-light);
    }
    
    .transfer-item-disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }
    
    .transfer-item-title {
      font-weight: 500;
      font-size: 0.875rem;
    }
    
    .transfer-item-desc {
      font-size: 0.75rem;
      margin-top: 0.125rem;
    }
    
    .transfer-operation {
      display: flex;
      flex-direction: column;
      justify-content: center;
      align-items: center;
      padding: 1rem 0.5rem;
    }
    
    .btn.btn-icon {
      width: 2.5rem;
      height: 2.5rem;
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 0;
      border-radius: 50%;
      background: var(--tblr-bg-surface);
      border: 1px solid var(--tblr-border-color);
      color: var(--tblr-text-secondary);
      transition: all 0.2s ease;
    }
    
    .btn.btn-icon:hover:not(.btn-disabled) {
      background: var(--tblr-primary);
      color: var(--tblr-white);
      border-color: var(--tblr-primary);
    }
    
    .btn.btn-icon.btn-disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }
    
    .form-check-input.m-0 {
      margin: 0 !important;
    }
    
    .ms-2 {
      margin-left: 0.5rem !important;
    }
    
    .text-secondary {
      color: var(--tblr-text-secondary) !important;
    }
    
    .w-100 {
      width: 100%;
    }
    
    .flex-fill {
      flex: 1;
    }
    
    @media (max-width: 767.98px) {
      .transfer-container {
        flex-direction: column;
      }
      
      .transfer-operation {
        flex-direction: row;
        gap: 0.5rem;
        padding: 1rem 0;
      }
    }
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

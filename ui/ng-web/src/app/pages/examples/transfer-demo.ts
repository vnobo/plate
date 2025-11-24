import { Component } from '@angular/core';
import { TransferComponent, TransferItem } from '@app/plugins';
import { ChangeDetectionStrategy } from '@angular/core';

@Component({
  selector: 'app-transfer-demo',
  standalone: true,
  imports: [TransferComponent],
  template: `
    <div class="page">
      <div class="page-header d-flex flex-column flex-md-row align-items-md-center">
        <div class="flex-grow-1">
          <h2 class="page-title">Transfer Component</h2>
          <p class="text-secondary mb-0">
            A dual-column transfer list component for moving items between lists.
          </p>
        </div>
      </div>

      <div class="page-body">
        <div class="row">
          <div class="col">
            <div class="card">
              <div class="card-header">
                <h3 class="card-title">Basic Transfer Example</h3>
              </div>
              <div class="card-body">
                <tabler-transfer
                  [data]="transferData"
                  [(modelValue)]="selectedKeys"
                  [titles]="['Move to right', 'Move to left']"
                  leftTitle="Available"
                  rightTitle="Selected"
                  (change)="onTransferChange($event)"
                />
              </div>
            </div>
          </div>
        </div>

        <div class="row mt-4">
          <div class="col-12">
            <div class="card">
              <div class="card-header">
                <h3 class="card-title">Transfer with Descriptions</h3>
              </div>
              <div class="card-body">
                <tabler-transfer
                  [data]="transferDataWithDescriptions"
                  [(modelValue)]="selectedKeys2"
                  [titles]="['Add to favorites', 'Remove from favorites']"
                  leftTitle="All Items"
                  rightTitle="Favorites"
                  (change)="onTransferChange2($event)"
                />
              </div>
            </div>
          </div>
        </div>

        <div class="row mt-4">
          <div class="col-12">
            <div class="card">
              <div class="card-header">
                <h3 class="card-title">Transfer with Disabled Items</h3>
              </div>
              <div class="card-body">
                <tabler-transfer
                  [data]="transferDataWithDisabled"
                  [(modelValue)]="selectedKeys3"
                  [titles]="['Add', 'Remove']"
                  leftTitle="Available Items"
                  rightTitle="Selected Items"
                  (change)="onTransferChange3($event)"
                />
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: `
    .page {
      padding: 1.5rem;
    }
    
    .card {
      margin-bottom: 1.5rem;
    }
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TransferDemoComponent {
  // Basic transfer data
  transferData: TransferItem[] = [
    { key: '1', title: 'Option 1' },
    { key: '2', title: 'Option 2' },
    { key: '3', title: 'Option 3' },
    { key: '4', title: 'Option 4' },
    { key: '5', title: 'Option 5' },
  ];

  selectedKeys: string[] = ['2', '4'];

  // Transfer data with descriptions
  transferDataWithDescriptions: TransferItem[] = [
    { key: '1', title: 'Email Notification', description: 'Receive notifications via email' },
    { key: '2', title: 'SMS Notification', description: 'Receive notifications via SMS' },
    { key: '3', title: 'Push Notification', description: 'Receive notifications via app' },
    { key: '4', title: 'In-App Notification', description: 'Receive notifications inside the app' },
    { key: '5', title: 'Desktop Notification', description: 'Receive notifications on desktop' },
  ];

  selectedKeys2: string[] = ['1', '3'];

  // Transfer data with disabled items
  transferDataWithDisabled: TransferItem[] = [
    { key: '1', title: 'Available Item 1', disabled: false },
    { key: '2', title: 'Available Item 2', disabled: false },
    { key: '3', title: 'Disabled Item', disabled: true },
    { key: '4', title: 'Available Item 3', disabled: false },
    { key: '5', title: 'Another Disabled Item', disabled: true },
  ];

  selectedKeys3: string[] = ['2'];

  onTransferChange(event: { source: TransferItem[]; target: TransferItem[] }) {
    console.log('Transfer change event:', event);
  }

  onTransferChange2(event: { source: TransferItem[]; target: TransferItem[] }) {
    console.log('Transfer with descriptions change event:', event);
  }

  onTransferChange3(event: { source: TransferItem[]; target: TransferItem[] }) {
    console.log('Transfer with disabled items change event:', event);
  }
}

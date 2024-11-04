import { Component, inject } from '@angular/core';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzCardModule } from 'ng-zorro-antd/card';

@Component({
  selector: 'exception-trigger',
  template: `
    <div class="pt-lg">
      <nz-card>
        @for (t of types; track $index) {
        <button (click)="go(t)" nz-button nzDanger>触发{{ t }}</button>
        }
        <button nz-button nzType="link" (click)="refresh()">触发刷新Token</button>
      </nz-card>
    </div>
  `,
  standalone: true,
  imports: [NzCardModule, NzButtonModule],
})
export class ExceptionTriggerComponent {
  types = [401, 403, 404, 500];

  go(type: number): void {}

  refresh(): void {}
}

import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-exception',
  template: ` <div>{{ type() }}</div>`,
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: true,
})
export class ExceptionComponent {
  private readonly route = inject(ActivatedRoute);

  get type() {
    return this.route.snapshot.data['type'];
  }
}

import { AsyncPipe } from '@angular/common';
import { Component } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { RouterOutlet } from '@angular/router';

import { NzBackTopModule } from 'ng-zorro-antd/back-top';
import { NzSpinModule } from 'ng-zorro-antd/spin';
import { LoadingService } from '../core/loading.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, AsyncPipe, NzBackTopModule, NzSpinModule],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss',
})
export class AppComponent {
  loadingShow = toSignal(this.loading.progress$, { initialValue: true });

  constructor(private loading: LoadingService) {}
}

import {Component} from '@angular/core';
import {RouterModule} from '@angular/router';

@Component({
  selector: 'app-layout-base',
  imports: [RouterModule],
  template: `<div class="page">
    <div class="page-wrapper">
      <div class="page-header d-print-none">
        <div class="container-xl">
          <div class="row g-2 align-items-center">
            <div class="col">
              <h2 class="page-title">Vertical layout</h2>
            </div>
          </div>
        </div>
      </div>
      <div class="page-body"><router-outlet></router-outlet></div>
    </div>
  </div>`,
  styles: [``],
})
export class BaseLayout {}

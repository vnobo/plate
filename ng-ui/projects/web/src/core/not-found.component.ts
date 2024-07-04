import {Component} from '@angular/core';
import {NzResultModule} from 'ng-zorro-antd/result';

@Component({
  selector: 'app-not-found',
  standalone: true,
  imports: [NzResultModule],
  template: `
    <nz-result
      nzStatus="403"
      nzSubTitle="Sorry, you are not authorized to access this page."
      nzTitle="403">
      <div class="" nz-result-extra>
        <a href="/home" nz-button nzType="primary">Back Home</a>
      </div>
    </nz-result>
  `,
  styles: `
    :host {
      min-height: 100%;
      min-width: 100%;
    }
  `,
})
export class NotFoundComponent {
}

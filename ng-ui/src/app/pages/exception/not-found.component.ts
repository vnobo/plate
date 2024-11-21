import { Component, type OnInit } from '@angular/core';
import { NzResultModule } from 'ng-zorro-antd/result';

@Component({
    selector: 'app-page-not-found',
    imports: [NzResultModule],
    template: `<nz-result nzStatus="404" nzSubTitle="Sorry, you are not authorized to access this page." nzTitle="404">
    <div class="" nz-result-extra>
      <a href="/home" nz-button nzType="primary">Back Home</a>
    </div>
  </nz-result> `,
    styles: [
        `
      :host {
        display: block;
        min-height: 100%;
        min-width: 100%;
      }
    `,
    ]
})
export class PageNotFoundComponent implements OnInit {
  ngOnInit(): void {}
}

import { Component, OnInit } from '@angular/core';

import { NzPageHeaderModule } from 'ng-zorro-antd/page-header';
import { NzTagModule } from 'ng-zorro-antd/tag';
import { NzSpaceModule } from 'ng-zorro-antd/space';
import { SharedModule } from '@app/shared/shared.module';

@Component({
  selector: 'page-header,[pageHeader]',
  standalone: true,
  imports: [NzPageHeaderModule, NzTagModule, NzSpaceModule, SharedModule],
  template: ` <div class="sticky-top">
    <nz-page-header [nzGhost]="false" nzBackIcon>
      <nz-breadcrumb [nzAutoGenerate]="true" [nzRouteLabel]="'title'" nz-page-header-breadcrumb></nz-breadcrumb>
      <nz-page-header-title>
        <ng-content select="[pageHeaderTitle]"></ng-content>
      </nz-page-header-title>
      <nz-page-header-subtitle>
        <ng-content select="[pageHeaderSubtitle]"></ng-content>
      </nz-page-header-subtitle>
      <nz-page-header-tags>
        <ng-content select="[pageHeaderTags]"></ng-content>
      </nz-page-header-tags>
      <nz-page-header-extra>
        <ng-content select="[pageHeaderExtra]"></ng-content>
      </nz-page-header-extra>
      <nz-page-header-content>
        <ng-content select="[pageHeaderContent]"></ng-content>
      </nz-page-header-content>
    </nz-page-header>
  </div>`,
})
export class PageHeaderComponent implements OnInit {
  constructor() {}

  ngOnInit() {}
}

import {Component} from '@angular/core';
import {LayoutService} from "../layout.service";

@Component({
  selector: 'app-page-not-found',
  templateUrl: './page-not-found.component.html',
  styleUrls: ['./page-not-found.component.scss']
})
export class PageNotFoundComponent {
  constructor(private layoutService: LayoutService) {
    layoutService.isHide(true);
  }
}

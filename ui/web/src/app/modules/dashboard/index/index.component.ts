import {Component} from '@angular/core';
import {IndexService} from "./index.service";

@Component({
  selector: 'app-dashboard',
  templateUrl: './index.component.html',
  styleUrls: ['./index.component.scss']
})
export class IndexComponent {

  constructor(private service: IndexService) {
  }

}

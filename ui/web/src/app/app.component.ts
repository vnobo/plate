import {Component, OnInit} from '@angular/core';
import {LoadingService} from "./core/loading.service";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit {

  loadingShow: boolean;

  constructor(private loading: LoadingService) {
    this.loadingShow = false;
  }

  ngOnInit(): void {
    this.loading.progress$.subscribe(res => this.loadingShow = res);
  }

}

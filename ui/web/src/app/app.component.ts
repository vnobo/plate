import {Component, OnInit} from '@angular/core';
import {LoadingService} from "./core/loading.service";
import {Observable} from "rxjs";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit {

  loadingShow$: Observable<boolean> | undefined;

  constructor(private loading: LoadingService) {
  }

  ngOnInit(): void {
    this.loadingShow$ = this.loading.progress$;
  }

}

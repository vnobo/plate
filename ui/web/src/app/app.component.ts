import {Component, OnInit} from '@angular/core';
import {ProgressBarService} from "./shared/progress-bar.service";
import {LayoutService} from "./shared/layout.service";
import {defaultIfEmpty, delay, tap} from "rxjs";
import {AuthService} from "./security/auth.service";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html'
})
export class AppComponent implements OnInit {

  progressShow = false;
  layoutHided = true;
  authenticated = false;

  constructor(private progress: ProgressBarService,
              private layoutService: LayoutService,
              private auth: AuthService) {
  }

  ngOnInit(): void {
    this.progress.progress$.pipe(
      defaultIfEmpty(false),
      delay(10),
      tap(res => console.log(`Progress is`, res))
    ).subscribe((res) => this.progressShow = res);

    this.layoutService.layoutHide$.pipe(
      defaultIfEmpty(false),
      delay(20),
      tap(res => console.log(`Layout is`, res))
    ).subscribe((res) => this.layoutHided = res)

    this.auth.authenticated$.pipe(
      defaultIfEmpty(false),
      delay(40),
      tap(res => console.log(`Authenticated is`, res))
    ).subscribe(res => this.authenticated = res);
  }

}

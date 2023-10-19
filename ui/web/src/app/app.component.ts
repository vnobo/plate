import {Component, OnInit} from '@angular/core';

@Component({
  // Selector for the component
  selector: 'app-root',
  // Template URL for the component
  templateUrl: './app.component.html',
  // Styles URL for the component
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit {
  page: number = 0;

  ngOnInit(): void {
    this.page = 1;
  }

}

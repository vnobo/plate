import { Component, OnInit } from '@angular/core';
import { RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet],
  template: `<router-outlet></router-outlet>`,
  host: {
    width: '100%',
    height: '100%',
  },
})
export class App implements OnInit {
  ngOnInit(): void {}
}

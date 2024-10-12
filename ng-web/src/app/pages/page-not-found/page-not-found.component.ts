import { Component, type OnInit } from '@angular/core';
import { NzResultModule } from 'ng-zorro-antd/result';

@Component({
  selector: 'app-page-not-found',
  standalone: true,
  imports: [NzResultModule],
  templateUrl: './page-not-found.component.html',
  styleUrl: './page-not-found.component.scss',
})
export class PageNotFoundComponent implements OnInit {
  ngOnInit(): void {
  }
}

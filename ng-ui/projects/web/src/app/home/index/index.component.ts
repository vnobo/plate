import { Component, OnInit, signal } from '@angular/core';
import { RouterModule } from '@angular/router';
import { PageHeaderComponent } from '../../../core/components/page-header/page-header.component';
import { MessageOut, RSocketCLientService } from '../../../core/rsocket.service';

@Component({
  selector: 'home-index',
  standalone: true,
  imports: [PageHeaderComponent, RouterModule],
  templateUrl: './index.component.html',
  styleUrl: './index.component.scss',
})
export class IndexComponent implements OnInit {
  dataSet = signal([] as MessageOut[]);

  constructor(private rsocket: RSocketCLientService) {}

  ngOnInit(): void {
    this.rsocket.requestStream().subscribe(res => this.dataSet().push(res));
  }
}

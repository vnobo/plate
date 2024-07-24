import { Component, OnInit, signal } from '@angular/core';
import { RouterModule } from '@angular/router';
import { PageHeaderComponent } from '../../../core/components/page-header/page-header.component';
import { MessageOut, RSocketCLientService } from '../../../core/rsocket.service';
import { NgFor } from '@angular/common';

@Component({
  selector: 'home-index',
  standalone: true,
  imports: [PageHeaderComponent, RouterModule, NgFor],
  templateUrl: './index.component.html',
  styleUrl: './index.component.scss',
})
export class IndexComponent implements OnInit {
  dataSet = signal([] as MessageOut[]);

  constructor(private _rsocket: RSocketCLientService) {}

  ngOnInit(): void {
    this._rsocket.requestStream('request.stream').subscribe(res => this.dataSet.update(value => [...value, res]));
  }
}

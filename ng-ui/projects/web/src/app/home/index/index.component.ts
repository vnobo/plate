import { Component, OnDestroy, OnInit, signal } from '@angular/core';
import { RouterModule } from '@angular/router';
import { PageHeaderComponent } from '../../../core/components/page-header/page-header.component';
import { MessageOut, RSocketCLientService } from '../../../core/rsocket.service';
import { NgFor } from '@angular/common';
import { Subject } from 'rxjs/internal/Subject';
import { debounceTime, distinctUntilChanged, takeUntil } from 'rxjs/operators';

@Component({
  selector: 'home-index',
  standalone: true,
  imports: [PageHeaderComponent, RouterModule, NgFor],
  templateUrl: './index.component.html',
  styleUrl: './index.component.scss',
})
export class IndexComponent implements OnInit, OnDestroy {
  dataSet = signal([] as MessageOut[]);

  private componentDestroyed$: Subject<void> = new Subject<void>();

  constructor(private _rsocket: RSocketCLientService) {}

  ngOnInit(): void {
    this._rsocket
      .requestStream('request.stream')
      .pipe(takeUntil(this.componentDestroyed$), debounceTime(100), distinctUntilChanged())
      .subscribe(res => this.dataSet.update(value => [...value, res]));
  }

  ngOnDestroy(): void {
    this.componentDestroyed$.next();
    this.componentDestroyed$.complete();
  }
}

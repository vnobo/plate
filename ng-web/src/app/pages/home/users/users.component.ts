import { CommonModule } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  inject,
  OnDestroy,
  type OnInit,
  signal,
  WritableSignal,
} from '@angular/core';
import { NzTableModule } from 'ng-zorro-antd/table';
import { Subject, takeUntil, tap } from 'rxjs';
import { UsersService } from './users.service';
import { NzNotificationModule, NzNotificationService } from 'ng-zorro-antd/notification';
import { User } from './user.types';
import { Page } from '../../../../types';

@Component({
  selector: 'app-users',
  standalone: true,
  imports: [CommonModule, NzTableModule, NzNotificationModule],
  templateUrl: './users.component.html',
  styleUrl: './users.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UsersComponent implements OnInit, OnDestroy {
  private _subject: Subject<void> = new Subject<void>();
  userPage: WritableSignal<Page<User>> = signal({} as Page<User>);
  private _message = inject(NzNotificationService);

  constructor(private _userSer: UsersService) {
  }

  ngOnInit(): void {
    this.refresh();
  }

  refresh() {
    this.loadData().subscribe(res =>
      this._message.success('数据刷新成功!', ``, { nzDuration: 1000 })
    );
  }

  loadData() {
    const request = {
      pcode: '0',
      tenantCode: '0',
    };
    const page = {
      pageNumber: 0,
      pageSize: 10,
      sorts: ['id,desc', 'name,desc'],
    };
    return this._userSer.pageUsers(request, page).pipe(
      takeUntil(this._subject),
      tap(result => this.userPage.set(result)),
    );
  }
  ngOnDestroy(): void {
    this._subject.next();
    this._subject.complete();
  }
}

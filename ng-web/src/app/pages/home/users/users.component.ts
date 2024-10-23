import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, OnDestroy, type OnInit, signal, WritableSignal } from '@angular/core';
import { NzTableModule, NzTableQueryParams } from 'ng-zorro-antd/table';
import { Subject, takeUntil, tap } from 'rxjs';
import { UsersService } from './users.service';
import { NzNotificationModule, NzNotificationService } from 'ng-zorro-antd/notification';
import { User } from './user.types';
import { Page, Pageable } from '../../../../types';
import { NzPaginationModule } from 'ng-zorro-antd/pagination';

@Component({
  selector: 'app-users',
  standalone: true,
  imports: [CommonModule, NzTableModule, NzNotificationModule, NzPaginationModule],
  templateUrl: './users.component.html',
  styleUrl: './users.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UsersComponent implements OnInit, OnDestroy {
  private _subject: Subject<void> = new Subject<void>();
  userPage: WritableSignal<Page<User>> = signal({} as Page<User>);
  page = {
    pageNumber: 1,
    pageSize: 10,
    sorts: ['id,desc'],
  };

  search = {
    pcode: '0',
    tenantCode: '0',
  };

  constructor(private _userSer: UsersService, private _message: NzNotificationService) {
  }

  ngOnInit(): void {
    this.refresh();
  }

  refresh() {
    this.loadData(this.search, this.page).subscribe(res => this._message.success('数据刷新成功!', ``, { nzDuration: 1000 }));
  }

  onQueryParamsChange($event: NzTableQueryParams) {
    this.page.sorts = ['id,desc'];
    for (var item in $event.sort) {
      var sort = $event.sort[item].key + ',' + ($event.sort[item].value == 'descend' ? 'desc' : 'asc');
      this.page.sorts.push(sort);
    }
    this.refresh();
  }

  loadData(search: User, page: Pageable) {
    return this._userSer.pageUsers(search, page).pipe(
      takeUntil(this._subject),
      tap(result => this.userPage.set(result)),
    );
  }
  ngOnDestroy(): void {
    this._subject.next();
    this._subject.complete();
  }
}

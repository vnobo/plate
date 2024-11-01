import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, OnDestroy, type OnInit, signal, WritableSignal } from '@angular/core';
import { ClipboardModule } from '@angular/cdk/clipboard';
import { NzTableModule, NzTableQueryParams } from 'ng-zorro-antd/table';
import { NzNotificationModule, NzNotificationService } from 'ng-zorro-antd/notification';
import { NzPaginationModule } from 'ng-zorro-antd/pagination';
import { Subject, takeUntil, tap } from 'rxjs';
import { UsersService } from './users.service';
import { User } from './user.types';
import { Page, Pageable } from '../../../core/types';
import { TruncateMiddlePipe } from '../../../shared/truncate-middle.pipe';
import { NzToolTipModule } from 'ng-zorro-antd/tooltip';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { FormsModule } from '@angular/forms';
import { NzPageHeaderModule } from 'ng-zorro-antd/page-header';
import { NzAvatarModule } from 'ng-zorro-antd/avatar';
import { SharedModule } from '../../../shared/shared.module';

@Component({
  selector: 'app-users',
  standalone: true,
  imports: [SharedModule],
  templateUrl: './users.component.html',
  styleUrl: './users.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UsersComponent implements OnInit, OnDestroy {
  private _subject: Subject<void> = new Subject<void>();
  userPage: WritableSignal<Page<User>> = signal({} as Page<User>);
  page = {
    page: 1,
    size: 10,
    sorts: ['id,desc'],
  };

  search = {
    search: '',
    pcode: '0',
    tenantCode: '0',
  };

  constructor(private _userSer: UsersService, private _message: NzNotificationService) {}

  ngOnInit(): void {}

  onSearch() {
    this.loadData(this.search, this.page).subscribe(res => this._message.success('数据加载成功!', ``, { nzDuration: 3000 }));
  }

  onQueryParamsChange($event: NzTableQueryParams) {
    this.page.sorts = [];
    for (const item of $event.sort) {
      if (item.value && item.value != null) {
        const sort = item.key + ',' + (item.value == 'descend' ? 'desc' : 'asc');
        this.page.sorts.push(sort);
      }
    }
    this.onSearch();
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

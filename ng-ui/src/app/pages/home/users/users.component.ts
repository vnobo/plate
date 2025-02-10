import {ChangeDetectionStrategy, Component, inject, signal} from '@angular/core';
import {Page, Pageable} from '@app/core/types';
import {NzModalService} from 'ng-zorro-antd/modal';
import {NzNotificationService} from 'ng-zorro-antd/notification';
import {NzTableQueryParams} from 'ng-zorro-antd/table';
import {tap} from 'rxjs';

import {DatePipe} from '@angular/common';
import {UserFormComponent} from '@app/pages';
import {SHARED_IMPORTS} from '@app/shared/shared-imports';
import {TruncateMiddlePipe} from '@app/shared/truncate-middle.pipe';
import {User} from './user.types';
import {UsersService} from './users.service';

@Component({
  selector: 'app-users',
  imports: [DatePipe, TruncateMiddlePipe, ...SHARED_IMPORTS],
  templateUrl: './users.component.html',
  styleUrl: './users.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UsersComponent {
  private readonly _message = inject(NzNotificationService);
  private readonly _modal = inject(NzModalService);
  private readonly _userSer = inject(UsersService);

  userPage = signal({} as Page<User>);
  page = signal({
    page: 1,
    size: 10,
    sorts: ['id,desc'],
  } as Pageable);
  search = signal({
    search: '',
    pcode: '0',
    tenantCode: '0',
  } as User);

  onSearch() {
    this.loadData(this.search(), this.page()).subscribe(() => this._message.success('数据加载成功!', ``, { nzDuration: 3000 }));
  }

  onTableQueryChange($event: NzTableQueryParams) {
    this.page().sorts = [];
    for (const item of $event.sort) {
      if (item.value) {
        const sort = item.key + ',' + (item.value == 'descend' ? 'desc' : 'asc');
        this.page().sorts.push(sort);
      }
    }
    this.onSearch();
  }

  openUserForm(user: User) {
    const modal = this._modal.create<UserFormComponent, User>({
      nzTitle: '用户表单',
      nzContent: UserFormComponent,
      nzFooter: null,
      nzZIndex: 2000,
    });
    const ref = modal.getContentComponent();
    ref.userData.set(user);
    ref.formSubmit.subscribe(us => {
      if (us.code) {
        this._userSer.modify(us).subscribe(res => {
          console.debug(`修改用户成功, 编码:${res.code}`);
          modal.close();
          this.onSearch();
        });
      } else {
        this._userSer.add(us).subscribe(res => {
          console.debug(`添加用户成功, 编码:${res.code}`);
          modal.close();
          this.onSearch();
        });
      }
    });
  }

  private loadData(search: User, page: Pageable) {
    return this._userSer.page(search, page).pipe(tap(result => this.userPage.set(result)));
  }
}

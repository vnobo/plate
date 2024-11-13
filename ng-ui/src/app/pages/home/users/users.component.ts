import { ChangeDetectionStrategy, Component, effect, inject, type OnInit, signal, untracked } from '@angular/core';
import { NzTableQueryParams } from 'ng-zorro-antd/table';
import { NzNotificationService } from 'ng-zorro-antd/notification';
import { tap } from 'rxjs';
import { SharedModule } from '@app/shared/shared.module';
import { Page, Pageable } from '@app/core/types';
import { NzPageHeaderModule } from 'ng-zorro-antd/page-header';
import { NzTagModule } from 'ng-zorro-antd/tag';
import { NzSpaceModule } from 'ng-zorro-antd/space';
import { NzModalService } from 'ng-zorro-antd/modal';

import { UsersService } from './users.service';
import { User } from './user.types';
import { UserFormComponent } from './user-form/user-form.component';

@Component({
  selector: 'app-users',
  standalone: true,
  imports: [SharedModule, NzPageHeaderModule, NzTagModule, NzSpaceModule],
  templateUrl: './users.component.html',
  styleUrl: './users.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UsersComponent implements OnInit {
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
  private readonly _message = inject(NzNotificationService);
  private readonly _modal = inject(NzModalService);
  private readonly _userSer = inject(UsersService);

  constructor() {
    effect(() => {
      untracked(() => {
        this.onSearch();
      });
    });
  }

  ngOnInit(): void {
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
    ref.formSubmit.subscribe(user => {
      if (user.id && user.id > 0) {
        this._userSer.modify(user).subscribe(res => {
          console.debug(`修改用户成功, ID: ${res.id},编码:${res.code}`);
          modal.close();
          this.onSearch();
        });
      } else {
        this._userSer.add(user).subscribe(res => {
          console.debug(`添加用户成功, ID: ${res.id},编码:${res.code}`);
          modal.close();
          this.onSearch();
        });
      }
    });
  }

  onSearch() {
    this.loadData(this.search(), this.page()).subscribe(() =>
      this._message.success('数据加载成功!', ``, { nzDuration: 3000 }));
  }

  onQueryParamsChange($event: NzTableQueryParams) {
    this.page().sorts = [];
    for (const item of $event.sort) {
      if (item.value) {
        const sort = item.key + ',' + (item.value == 'descend' ? 'desc' : 'asc');
        this.page().sorts.push(sort);
      }
    }
  }

  loadData(search: User, page: Pageable) {
    return this._userSer.pageUsers(search, page).pipe(
      tap(result => this.userPage.set(result)),
    );
  }

}

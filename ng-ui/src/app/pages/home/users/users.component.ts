import {ChangeDetectionStrategy, Component, inject, signal} from '@angular/core';
import {Page, Pageable} from '@app/core/types';
import {NzModalService} from 'ng-zorro-antd/modal';
import {NzNotificationService} from 'ng-zorro-antd/notification';
import {NzTableQueryParams} from 'ng-zorro-antd/table';
import {delay, tap} from 'rxjs';

import {UserFormComponent} from '@app/pages';
import {SHARED_IMPORTS} from '@app/shared/shared-imports';
import {User} from './user.types';
import {UsersService} from './users.service';

@Component({
  selector: 'app-users',
  imports: [...SHARED_IMPORTS],
  templateUrl: './users.component.html',
  styleUrl: './users.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UsersComponent {
  private readonly _message = inject(NzNotificationService);
  private readonly _modal = inject(NzModalService);
  private readonly _userSer = inject(UsersService);

  userData = signal({} as Page<User>);

  page = signal({
    page: 1,
    size: 10,
    sorts: ['id,desc'],
  } as Pageable);

  search = signal({
    pcode: '0',
  } as User);

  fetchUserData() {
    this.loadData(this.search(), this.page()).subscribe(() => this._message.success('数据加载成功!', ``, { nzDuration: 3000 }));
  }

  onTableQueryChange($event: NzTableQueryParams) {
    this.page().sorts = ['id,desc'];
    for (const item of $event.sort) {
      if (item.value) {
        const sort = item.key + ',' + (item.value == 'descend' ? 'desc' : 'asc');
        this.page().sorts.push(sort);
      }
    }
    this.fetchUserData();
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
          modal.close();
          this._message.success('修改成功!', ``, { nzDuration: 3000 });
          this.fetchUserData();
        });
      } else {
        this._userSer.add(us).subscribe(res => {
          modal.close();
          this._message.success('添加成功!', ``, { nzDuration: 3000 });
          this.fetchUserData();
        });
      }
    });
  }
  onDelete(user: User) {
    this._userSer
      .delete(user)
      .pipe(
        tap(() => this._message.success('删除成功!', ``, { nzDuration: 3000 })),
        delay(1500),
      )
      .subscribe(() => this.fetchUserData());
  }

  private loadData(search: User, page: Pageable) {
    return this._userSer.page(search, page).pipe(tap(result => this.userData.set(result)));
  }
}

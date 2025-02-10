import {DatePipe} from '@angular/common';
import {Component, effect, inject, OnInit, signal, untracked} from '@angular/core';
import {NzModalService} from 'ng-zorro-antd/modal';
import {NzNotificationService} from 'ng-zorro-antd/notification';
import {NzTableQueryParams} from 'ng-zorro-antd/table';

import {tap} from 'rxjs';

import {Page, Pageable} from '@app/core/types';
import {MenuFormComponent, MenusService} from '@app/pages';
import {User} from '@app/pages/home/users/user.types';
import {SHARED_IMPORTS} from '@app/shared/shared-imports';
import {Menu} from './menu.types';
import {TruncateMiddlePipe} from '@app/shared/truncate-middle.pipe';

@Component({
  selector: 'app-menus',
  templateUrl: './menus.component.html',
  styleUrls: ['./menus.component.scss'],
  imports: [DatePipe, TruncateMiddlePipe, ...SHARED_IMPORTS],
})
export class MenusComponent implements OnInit {
  private readonly _modal = inject(NzModalService);
  private readonly _menusSer = inject(MenusService);
  private readonly _message = inject(NzNotificationService);
  menuPage = signal({} as Page<Menu>);
  page = signal({
    page: 1,
    size: 10,
    sorts: ['id,desc'],
  } as Pageable);
  search = signal({
    pcode: '00000000-0000-0000-0000-000000000000',
    tenantCode: '0',
  } as User);

  mapOfExpandedData: Record<string, Menu[]> = {};

  constructor() {
    effect(() => {
      untracked(() => {
        //todo: 信号优化数据流
      });
    });
  }

  ngOnInit(): void {
    this.onSearch();
  }

  openMenuForm(menu: Menu) {
    const modal = this._modal.create<MenuFormComponent, Menu>({
      nzTitle: '菜单表单',
      nzContent: MenuFormComponent,
      nzFooter: null,
      nzZIndex: 2000,
    });
    const ref = modal.getContentComponent();
    ref.menuData.set(menu);
    ref.formSubmit.subscribe(ms => {
      this._menusSer.save(ms).subscribe(res => {
        console.debug(`保存菜单成功,编码:${res.code}`);
        modal.close();
        this.onSearch();
      });
    });
  }

  delete(menu: Menu) {
    this._menusSer.delete(menu).subscribe(() => this.onSearch());
  }

  onTableQueryChange($event: NzTableQueryParams) {
    this.page().sorts = [];
    for (const item of $event.sort) {
      if (item.value) {
        const sort = item.key + ',' + (item.value == 'descend' ? 'desc' : 'asc');
        this.page().sorts.push(sort);
      }
    }
  }

  onSearch() {
    this.loadData(this.search(), this.page()).subscribe(() => this._message.success('数据加载成功!', ``, { nzDuration: 3000 }));
  }

  collapse(array: Menu[], data: Menu, $event: boolean): void {
    if (!$event) {
      if (data.children) {
        data.children.forEach(d => {
          const target = array.find(a => a.code === d.code);
          if (target) {
            target.expand = false;
            this.collapse(array, target, false);
          }
        });
      }
    }
  }

  convertTreeToList(root: Menu): Menu[] {
    const stack: Menu[] = [];
    const array: Menu[] = [];
    const hashMap = {};
    stack.push({ ...root, level: 0, expand: false });
    while (stack.length !== 0) {
      const node = stack.pop();
      if (!node) {
        continue;
      }
      this.visitNode(node, hashMap, array);
      if (node.children) {
        for (let i = node.children.length - 1; i >= 0; i--) {
          stack.push({
            ...node.children[i],
            level: node.level ? node.level + 1 : 1,
            expand: false,
            parent: node,
          });
        }
      }
    }
    return array;
  }

  visitNode(node: Menu, hashMap: Record<string, boolean>, array: Menu[]): void {
    if (!hashMap[node.code ? node.code : '00000000-0000-0000-0000-000000000000']) {
      hashMap[node.code ? node.code : '00000000-0000-0000-0000-000000000000'] = true;
      array.push(node);
    }
  }

  private loadData(search: Menu, page: Pageable) {
    return this._menusSer.page(search, page).pipe(
      tap(result => {
        this.menuPage.set(result);
        this.menuPage().content.forEach(item => {
          this.mapOfExpandedData[item.code ? item.code : '0'] = this.convertTreeToList(item);
        });
      }),
    );
  }
}

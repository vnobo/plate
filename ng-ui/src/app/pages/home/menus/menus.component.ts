import { Component, inject, OnDestroy, OnInit, signal, WritableSignal } from '@angular/core';
import { CommonModule } from '@angular/common';

import { NzTableModule } from 'ng-zorro-antd/table';
import { NzDividerModule } from 'ng-zorro-antd/divider';
import { NzPopconfirmModule } from 'ng-zorro-antd/popconfirm';
import { NzNotificationModule, NzNotificationService } from 'ng-zorro-antd/notification';

import { MenusService } from '@app/pages';
import { Subject, takeUntil, tap } from 'rxjs';
import { Menu } from './menu.types';
import { MenuFormComponent } from './menu-form.component';

@Component({
  selector: 'app-menus',
  standalone: true,
  templateUrl: './menus.component.html',
  styleUrls: ['./menus.component.scss'],
  imports: [
    CommonModule,
    NzTableModule,
    MenuFormComponent,
    NzPopconfirmModule,
    NzDividerModule,
    NzNotificationModule,
  ],
})
export class MenusComponent implements OnInit, OnDestroy {
  listMenus: WritableSignal<Menu[]> = signal([]);
  mapOfExpandedData: Record<string, Menu[]> = {};
  private _menusSer = inject(MenusService);
  private _message = inject(NzNotificationService);
  private _subject: Subject<void> = new Subject<void>();

  ngOnInit(): void {
    this.loadData().subscribe();
  }

  formEvent($event: any): void {
    if ($event.btn === 'submit' && $event.status === 100) {
      const menu = $event.data as Menu;
      this._menusSer.saveMenu(menu).subscribe(result => this.refresh());
    }
  }

  delete(menu: Menu) {
    this._menusSer
      .deleteMenu(menu)
      .pipe(takeUntil(this._subject))
      .subscribe(() => this.refresh());
  }

  refresh() {
    this.loadData().subscribe(res =>
      this._message.success('数据刷新成功!', ``, { nzDuration: 1000 }),
    );
  }

  loadData() {
    const menuRequest: Menu = {
      pcode: '0',
      tenantCode: '0',
    };
    return this._menusSer.getMenus(menuRequest).pipe(
      takeUntil(this._subject),
      tap(result => {
        this.listMenus.set(result);
        this.listMenus().forEach(item => {
          this.mapOfExpandedData[item.code ? item.code : '0'] = this.convertTreeToList(item);
        });
      }),
    );
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
    if (!hashMap[node.code ? node.code : '0']) {
      hashMap[node.code ? node.code : '0'] = true;
      array.push(node);
    }
  }

  ngOnDestroy(): void {
    this._subject.next();
    this._subject.complete();
  }
}

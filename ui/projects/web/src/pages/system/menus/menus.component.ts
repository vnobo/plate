import {Component, OnDestroy, OnInit} from '@angular/core';
import {Menu, MenusService} from './menus.service';
import {Subject, takeUntil} from 'rxjs';

@Component({
  selector: 'app-menus',
  templateUrl: './menus.component.html',
  styleUrls: ['./menus.component.scss'],
})
export class MenusComponent implements OnInit, OnDestroy {
  listMenus: Menu[] = [];
  mapOfExpandedData: { [key: string]: Menu[] } = {};

  private _subject: Subject<void> = new Subject<void>();

  constructor(private menusService: MenusService) {
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
      } else {
        return;
      }
    }
  }

  convertTreeToList(root: Menu): Menu[] {
    const stack: Menu[] = [];
    const array: Menu[] = [];
    const hashMap = {};
    stack.push({...root, level: 0, expand: false});
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

  visitNode(
    node: Menu,
    hashMap: { [code: string]: boolean },
    array: Menu[]
  ): void {
    if (!hashMap[node.code ? node.code : '0']) {
      hashMap[node.code ? node.code : '0'] = true;
      array.push(node);
    }
  }

  ngOnInit(): void {
    const menuRequest: Menu = {
      pcode: '0',
      tenantCode: '0',
    };
    this.menusService
      .getMenus(menuRequest)
      .pipe(takeUntil(this._subject))
      .subscribe(result => {
        this.listMenus = result;
        this.listMenus.forEach(item => {
          this.mapOfExpandedData[item.code ? item.code : '0'] =
            this.convertTreeToList(item);
        });
      });
  }

  ngOnDestroy(): void {
    this._subject.next();
    this._subject.complete();
  }
}

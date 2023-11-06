import {Component, OnInit} from '@angular/core';
import {MenusService} from "../../core/menus.service";
import {Menu} from "../../core/interfaces/menu";
import {Observable} from "rxjs";

@Component({
  selector: 'app-welcome',
  templateUrl: './index.component.html',
  styleUrls: ['./index.component.scss']
})
export class IndexComponent implements OnInit {

  menus$: Observable<Menu[]> | undefined;

  constructor(private menusService: MenusService) {
  }

  ngOnInit() {
    this.initMenu();
  }

  initMenu() {
    const menuRequest: Menu = {
      pcode: "0",
      tenantCode: "0"
    };
    this.menus$ = this.menusService.getMenus(menuRequest);
  }
}

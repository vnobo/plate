import {Component, OnInit} from '@angular/core';
import {Observable} from "rxjs";
import {Menu} from "../../core/interfaces/menu";
import {MenusService} from "../../core/menus.service";

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss']
})
export class HomeComponent implements OnInit {
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

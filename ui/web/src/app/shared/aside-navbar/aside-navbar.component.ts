import {Component, OnInit} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable, retry} from "rxjs";
import {AuthService} from "../../security/auth.service";

export interface Menu {
  code: string;
  pcode: string;
  name: string;
  type: string;
  authority: string;
  path: string;
  icons: string;
  sort: number;
}

@Component({
  selector: 'app-aside-navbar',
  templateUrl: './aside-navbar.component.html',
  styleUrls: ['./aside-navbar.component.scss']
})
export class AsideNavbarComponent implements OnInit {

  private menus: Array<Menu> | undefined;

  constructor(private http: HttpClient,
              private auth: AuthService) {
  }

  /**
   * This function returns an array of parent Menus or null
   * if the menus is undefined. It takes an array menus and
   * then loops over each menu and adds the folder type menus
   * to the list and returns the list sorted by the sort
   * value.
   *
   * @return {Menu[]|null}
   */
  parentMenus(): Menu[] {
    if (this.menus === undefined) {
      return [];
    }
    const parentMenus: Array<Menu> = [];
    for (const menu of this.menus) {
      if (menu.type === 'FOLDER') {
        parentMenus.push(menu);
      }
    }
    return parentMenus.sort((a, b) => a.sort - b.sort);
  }

  /*
    subMenus() returns a sorted array of Menu objects filtered from the given Menu object.
    In case the value of this.menus is undefined, it returns null.
    @param prent: Menu object to filter from
    @Return Menu[] | null
  */
  subMenus(prent: Menu): Menu[] {
    if (this.menus === undefined) {
      return [];
    }
    const prentMenus: Array<Menu> = [];
    for (const menu of this.menus) {
      if (menu.type === 'MENU' && menu.pcode === prent.code) {
        prentMenus.push(menu);
      }
    }
    return prentMenus.sort((a, b) => b.sort - a.sort);
  }

  ngOnInit(): void {
    this.loadMenus().subscribe(res => this.menus = res);
  }

  private loadMenus(): Observable<Menu[]> {
    return this.http.get<Menu[]>('/menus/me').pipe(retry(3));
  }
}

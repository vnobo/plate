import { RouterStateSnapshot, TitleStrategy } from '@angular/router';
import { Title } from '@angular/platform-browser';
import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class PageTitleStrategy extends TitleStrategy {
  constructor(private readonly title: Title) {
    super();
  }

  override updateTitle(routerState: RouterStateSnapshot) {
    let title = this.buildTitle(routerState);
    if (title == '' || title == null) {
      title = routerState.root.data['title'];
    }
    this.title.setTitle(`盘子管理平台 | ${title}`);
  }
}

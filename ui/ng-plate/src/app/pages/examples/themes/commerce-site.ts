import { NgOptimizedImage } from '@angular/common';
import { Component, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import {
  PASSTHROUGH_IMAGE_LOADER,
  showcaseImage,
  type ShowcaseImage,
} from './image-loader';
import { THEMES, type ThemeMeta } from './theme-meta';

interface Category {
  readonly image: ShowcaseImage;
  readonly title: string;
  readonly note: string;
}

interface Editorial {
  readonly image: ShowcaseImage;
  readonly kicker: string;
  readonly title: string;
  readonly text: string;
  readonly link: string;
  readonly reverse: boolean;
}

@Component({
  selector: 'app-commerce-site',
  imports: [RouterLink, NgOptimizedImage],
  providers: [PASSTHROUGH_IMAGE_LOADER],
  templateUrl: './commerce-site.html',
})
export class CommerceSite {
  protected readonly themes: ReadonlyArray<ThemeMeta> = THEMES;
  protected readonly menuOpen = signal(false);
  protected readonly subscribed = signal(false);

  protected readonly heroImage: ShowcaseImage = showcaseImage(
    'photo-1441986300917-64674bd600d8',
    1600,
    900,
    '暖色灯光下的生活方式买手店陈列',
  );

  protected readonly categories: ReadonlyArray<Category> = [
    {
      image: showcaseImage('photo-1490481651871-ab68de25d43d', 600, 800, '浅色系衣饰与织物的平铺陈列'),
      title: '衣饰',
      note: '天然纤维 · 26 件',
    },
    {
      image: showcaseImage('photo-1503602642458-232111445657', 600, 750, '木质座椅与柔和光影的家居单品'),
      title: '家居',
      note: '手工木作 · 18 件',
    },
    {
      image: showcaseImage('photo-1523275335684-37898b6baf30', 600, 800, '极简腕表与配饰特写'),
      title: '配饰',
      note: '日常佩戴 · 32 件',
    },
  ];

  protected readonly editorials: ReadonlyArray<Editorial> = [
    {
      image: showcaseImage('photo-1600607687939-ce8a6c25118c', 900, 675, '洒满自然光的客厅与亚麻织物'),
      kicker: 'Journal 01 / 慢下来',
      title: '一间屋子，如何容纳一整个下午',
      text: '我们把窗帘换成透光的亚麻，把桌面留空，只放一只用了三年的陶杯。空间不必被填满，留白才是让人愿意久坐的原因。',
      link: '阅读这一期',
      reverse: false,
    },
    {
      image: showcaseImage('photo-1616486338812-3dadae4b4ace', 900, 675, '暖色调室内与手工陶器的静物'),
      kicker: 'Journal 02 / 手感',
      title: '被使用过的器物，才有温度',
      text: '每一道细微的釉色差别，都来自匠人当天的手感。我们不追求完美的一致，那些不规律的地方，恰好是一件器物开始属于你的时刻。',
      link: '阅读这一期',
      reverse: true,
    },
  ];

  protected toggleMenu(): void {
    this.menuOpen.update((open) => !open);
  }

  protected subscribe(event: Event): void {
    event.preventDefault();
    this.subscribed.set(true);
  }
}

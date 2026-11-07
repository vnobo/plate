import { NgOptimizedImage } from '@angular/common';
import { Component, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import {
  PASSTHROUGH_IMAGE_LOADER,
  showcaseImage,
  type ShowcaseImage,
} from './image-loader';
import { THEMES, type ThemeMeta } from './theme-meta';

interface Project {
  readonly image: ShowcaseImage;
  readonly title: string;
  readonly category: string;
  readonly year: string;
}

interface Service {
  readonly title: string;
  readonly desc: string;
}

@Component({
  selector: 'app-studio-site',
  imports: [RouterLink, NgOptimizedImage],
  providers: [PASSTHROUGH_IMAGE_LOADER],
  templateUrl: './studio-site.html',
})
export class StudioSite {
  protected readonly themes: ReadonlyArray<ThemeMeta> = THEMES;
  protected readonly menuOpen = signal(false);

  protected readonly projects: ReadonlyArray<Project> = [
    {
      image: showcaseImage('photo-1541701494587-cb58502866ab', 900, 700, '高饱和流体色块构成的抽象视觉'),
      title: '曜石品牌重塑',
      category: '品牌识别',
      year: '2026',
    },
    {
      image: showcaseImage('photo-1550859492-d5da9d8e45f3', 700, 560, '粉紫渐变的抽象光影作品'),
      title: '流动的城市',
      category: '数字体验',
      year: '2025',
    },
    {
      image: showcaseImage('photo-1487958449943-2429e8be8625', 700, 560, '白色几何建筑立面的摄影'),
      title: '留白美术馆',
      category: '空间视觉',
      year: '2025',
    },
    {
      image: showcaseImage('photo-1524504388940-b1c1722653e1', 1200, 500, '高对比人像与光影的实验影像'),
      title: '无题 04',
      category: '实验影像',
      year: '2024',
    },
  ];

  protected readonly services: ReadonlyArray<Service> = [
    { title: '品牌策略与识别', desc: '从命名、叙事到一整套可生长的视觉系统。' },
    { title: '数字产品体验', desc: '网站、应用与交互原型，兼顾表达与可用性。' },
    { title: '影像与动效', desc: '把静态概念变成能被记住的一段运动。' },
    { title: '空间与展陈', desc: '让品牌在真实空间里同样有说服力。' },
  ];

  protected readonly awards: ReadonlyArray<string> = [
    'AWWWARDS',
    'CSS DESIGN AWARDS',
    'D&AD',
    'RED DOT',
    'BEHANCE FEATURED',
    'TYPE DIRECTORS CLUB',
  ];

  protected toggleMenu(): void {
    this.menuOpen.update((open) => !open);
  }
}

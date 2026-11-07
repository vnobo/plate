import { Component, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { THEMES, type ThemeMeta } from './theme-meta';

interface Dimension {
  readonly title: string;
  readonly desc: string;
}

@Component({
  selector: 'app-themes-gallery',
  imports: [RouterLink],
  templateUrl: './themes-gallery.html',
})
export class ThemesGallery {
  protected readonly themes: ReadonlyArray<ThemeMeta> = THEMES;
  protected readonly menuOpen = signal(false);

  protected readonly dimensions: ReadonlyArray<Dimension> = [
    {
      title: '配色',
      desc: '从深墨电光到暖米陶土，从荧光柠檬到海军蓝金，四套色板互不重叠，气质一眼可辨。',
    },
    {
      title: '字体',
      desc: '几何无衬线、优雅衬线、压缩粗体与稳健数字体，各自建立不同的阅读节奏与标题比例。',
    },
    {
      title: '组件',
      desc: '玻璃拟态卡片、细线编辑式图文、粗描边作品格与对称数据面板，连按钮形状都不一样。',
    },
  ];

  protected toggleMenu(): void {
    this.menuOpen.update((open) => !open);
  }
}

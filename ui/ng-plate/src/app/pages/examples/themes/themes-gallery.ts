import { Component, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { LocaleSwitcher } from '@app/layout/locale-switcher';
import { THEMES, type ThemeMeta } from './theme-meta';

interface Dimension {
  readonly title: string;
  readonly desc: string;
}

@Component({
  selector: 'app-themes-gallery',
  imports: [RouterLink, LocaleSwitcher],
  templateUrl: './themes-gallery.html',
})
export class ThemesGallery {
  protected readonly themes: ReadonlyArray<ThemeMeta> = THEMES;
  protected readonly menuOpen = signal(false);

  protected readonly dimensions: ReadonlyArray<Dimension> = [
    {
      title: $localize`:@@galleryDimensionColorTitle:配色`,
      desc: $localize`:@@galleryDimensionColorDesc:从电光墨蓝到暖陶土，从荧光柠檬黄到海军金，四套配色互不重叠，一眼即可分辨。`,
    },
    {
      title: $localize`:@@galleryDimensionTypographyTitle:字体`,
      desc: $localize`:@@galleryDimensionTypographyDesc:几何无衬线、优雅衬线、压缩粗体与稳健数字字体，各自定下独特的阅读节奏与标题层级。`,
    },
    {
      title: $localize`:@@galleryDimensionComponentsTitle:组件`,
      desc: $localize`:@@galleryDimensionComponentsDesc:玻璃拟态卡片、细线杂志版式、重边框作品集网格与对称数据面板，连按钮形态都各不相同。`,
    },
  ];

  protected toggleMenu(): void {
    this.menuOpen.update((open) => !open);
  }
}

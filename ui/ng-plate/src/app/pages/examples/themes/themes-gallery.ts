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
      title: 'Color',
      desc: 'From electric ink to warm clay, from fluorescent lemon to navy gold — four palettes that never overlap and read as distinct at a glance.',
    },
    {
      title: 'Typography',
      desc: 'Geometric sans-serif, elegant serif, condensed bold and sturdy numeral fonts each set their own reading rhythm and heading scale.',
    },
    {
      title: 'Components',
      desc: 'Glassmorphism cards, thin-line editorial layouts, heavy-bordered portfolio grids and symmetric data panels — even the button shapes differ.',
    },
  ];

  protected toggleMenu(): void {
    this.menuOpen.update((open) => !open);
  }
}

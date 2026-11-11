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
    'Lifestyle boutique display under warm lighting',
  );

  protected readonly categories: ReadonlyArray<Category> = [
    {
      image: showcaseImage('photo-1490481651871-ab68de25d43d', 600, 800, 'Flat-lay of light-toned garments and textiles'),
      title: 'Apparel',
      note: 'Natural fibers · 26 items',
    },
    {
      image: showcaseImage('photo-1503602642458-232111445657', 600, 750, 'Wooden chair and soft-light homeware piece'),
      title: 'Home',
      note: 'Handcrafted wood · 18 items',
    },
    {
      image: showcaseImage('photo-1523275335684-37898b6baf30', 600, 800, 'Minimalist watch and accessories close-up'),
      title: 'Accessories',
      note: 'Everyday wear · 32 items',
    },
  ];

  protected readonly editorials: ReadonlyArray<Editorial> = [
    {
      image: showcaseImage('photo-1600607687939-ce8a6c25118c', 900, 675, 'Living room bathed in natural light with linen textiles'),
      kicker: 'Journal 01 / Slow down',
      title: 'How a room holds an entire afternoon',
      text: 'We swapped the curtains for translucent linen and cleared the table, leaving only a three-year-old ceramic cup. A space need not be filled — the emptiness is what makes people want to stay.',
      link: 'Read this issue',
      reverse: false,
    },
    {
      image: showcaseImage('photo-1616486338812-3dadae4b4ace', 900, 675, 'Warm-toned interior still life with handmade pottery'),
      kicker: 'Journal 02 / Texture',
      title: 'Objects gain warmth only once used',
      text: 'Every subtle difference in glaze comes from the craftsperson’s hand that day. We do not chase perfect uniformity; the irregularities are exactly when an object begins to belong to you.',
      link: 'Read this issue',
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

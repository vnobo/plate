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
      image: showcaseImage('photo-1541701494587-cb58502866ab', 900, 700, 'Abstract visual built from saturated fluid color blocks'),
      title: 'Obsidian Rebrand',
      category: 'Brand Identity',
      year: '2026',
    },
    {
      image: showcaseImage('photo-1550859492-d5da9d8e45f3', 700, 560, 'Abstract light study in pink-purple gradient'),
      title: 'Fluid City',
      category: 'Digital Experience',
      year: '2025',
    },
    {
      image: showcaseImage('photo-1487958449943-2429e8be8625', 700, 560, 'Photograph of a white geometric building facade'),
      title: 'Negative Space Gallery',
      category: 'Spatial Visual',
      year: '2025',
    },
    {
      image: showcaseImage('photo-1524504388940-b1c1722653e1', 1200, 500, 'Experimental imagery of high-contrast portrait and light'),
      title: 'Untitled 04',
      category: 'Experimental Film',
      year: '2024',
    },
  ];

  protected readonly services: ReadonlyArray<Service> = [
    { title: 'Brand Strategy & Identity', desc: 'From naming and narrative to a visual system that keeps growing.' },
    { title: 'Digital Product Experience', desc: 'Websites, apps and interaction prototypes that balance expression and usability.' },
    { title: 'Film & Motion', desc: 'Turning static concepts into a movement people remember.' },
    { title: 'Space & Exhibition', desc: 'Making the brand just as convincing in physical space.' },
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

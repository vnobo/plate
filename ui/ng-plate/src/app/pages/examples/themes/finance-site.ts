import { NgOptimizedImage } from '@angular/common';
import { Component, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import {
  PASSTHROUGH_IMAGE_LOADER,
  showcaseImage,
  type ShowcaseImage,
} from './image-loader';
import { THEMES, type ThemeMeta } from './theme-meta';

interface Panel {
  readonly label: string;
  readonly value: string;
  readonly note: string;
}

interface Service {
  readonly icon: string;
  readonly title: string;
  readonly desc: string;
}

interface Metric {
  readonly value: string;
  readonly label: string;
}

interface Insight {
  readonly date: string;
  readonly category: string;
  readonly title: string;
}

@Component({
  selector: 'app-finance-site',
  imports: [RouterLink, NgOptimizedImage],
  providers: [PASSTHROUGH_IMAGE_LOADER],
  templateUrl: './finance-site.html',
})
export class FinanceSite {
  protected readonly themes: ReadonlyArray<ThemeMeta> = THEMES;
  protected readonly menuOpen = signal(false);

  protected readonly heroImage: ShowcaseImage = showcaseImage(
    'photo-1486406146926-c627a92ad1ab',
    1600,
    900,
    '',
  );

  protected readonly panels: ReadonlyArray<Panel> = [
    { label: 'Assets under management', value: '¥ 186B', note: '+12.4% YoY' },
    { label: 'Enterprise clients served', value: '420+', note: 'Across 18 industries' },
    { label: 'Average partnership', value: '8.6 yrs', note: 'Long-term partnership' },
  ];

  protected readonly services: ReadonlyArray<Service> = [
    {
      icon: 'M4 19h16M6 16V9m6 7V5m6 11v-4M4 19l0 1',
      title: 'Asset Management',
      desc: 'Rigorous asset allocation framework and full-process risk control to manage long-term capital for institutional and family clients.',
    },
    {
      icon: 'M3 10l9 -7l9 7M5 10v9h14v-9M9 19v-6h6v6',
      title: 'Corporate Finance',
      desc: 'Combining industry cycles and company stage to design sustainable financing structures and capital-market paths.',
    },
    {
      icon: 'M4 4h16v16H4zM8 14l3 -3l2 2l3 -4',
      title: 'Research & Insight',
      desc: '42 registered analysts continuously track macro and industry shifts, delivering verifiable judgments.',
    },
  ];

  protected readonly metrics: ReadonlyArray<Metric> = [
    { value: '1998', label: 'Founded' },
    { value: '18', label: 'Industries covered' },
    { value: '42', label: 'Registered analysts' },
    { value: '99.2%', label: 'Client renewal rate' },
  ];

  protected readonly insights: ReadonlyArray<Insight> = [
    { date: '2026.10.28', category: 'Macro Strategy', title: 'After the rate inflection: rebalancing duration and cash flow' },
    { date: '2026.10.15', category: 'Industry Research', title: 'Manufacturing capex cycle: from equipment renewal to capacity going global' },
    { date: '2026.09.30', category: 'Asset Allocation', title: 'Q4 outlook: keeping portfolio resilience amid uncertainty' },
    { date: '2026.09.12', category: 'ESG Watch', title: 'Impact of standardized sustainability disclosure on valuation' },
  ];

  protected toggleMenu(): void {
    this.menuOpen.update((open) => !open);
  }
}

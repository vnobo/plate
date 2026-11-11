import { NgOptimizedImage } from '@angular/common';
import { Component, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import {
  PASSTHROUGH_IMAGE_LOADER,
  showcaseImage,
  type ShowcaseImage,
} from './image-loader';
import { THEMES, type ThemeMeta } from './theme-meta';

interface Feature {
  readonly icon: string;
  readonly title: string;
  readonly desc: string;
}

interface Metric {
  readonly value: string;
  readonly label: string;
}

interface Chip {
  readonly label: string;
  readonly value: string;
}

@Component({
  selector: 'app-saas-site',
  imports: [RouterLink, NgOptimizedImage],
  providers: [PASSTHROUGH_IMAGE_LOADER],
  templateUrl: './saas-site.html',
})
export class SaasSite {
  protected readonly themes: ReadonlyArray<ThemeMeta> = THEMES;
  protected readonly menuOpen = signal(false);

  protected readonly heroImage: ShowcaseImage = showcaseImage(
    'photo-1618005182384-a83a8bd57fbe',
    720,
    560,
    'Blue-purple abstract gradient light, used as a data panel background',
  );

  protected readonly clients: ReadonlyArray<string> = [
    'Beichen Tech',
    'Yunqi Capital',
    'Atlas',
    'Lumen Labs',
    'Star River Data',
    'Nova Studio',
  ];

  protected readonly features: ReadonlyArray<Feature> = [
    {
      icon: 'M13 2l-1 7h6l-8 13l1 -9H5l8 -11z',
      title: 'Real-time Data Pipeline',
      desc: 'Sync 60+ business data sources in seconds — no cron scripts to maintain, so reports always land on the latest moment.',
    },
    {
      icon: 'M12 3l8 3v6c0 5 -3.5 8 -8 9c-4.5 -1 -8 -4 -8 -9V6l8 -3zM9 12l2 2l4 -4',
      title: 'Enterprise Security',
      desc: 'Field-level permissions, end-to-end audit and SSO, certified to SOC 2 Type II and MLPS Level 3.',
    },
    {
      icon: 'M4 4v16h16M8 15l3 -4l3 3l5 -7',
      title: 'Self-service Analytics',
      desc: 'Build funnel, retention and attribution analysis by dragging — no more queuing for the data team.',
    },
    {
      icon: 'M12 3l9 5l-9 5l-9 -5l9 -5zM3 13l9 5l9 -5M3 17l9 5l9 -5',
      title: 'Unified Metrics Layer',
      desc: 'Converge scattered definitions into one set of metrics, so the same number means the same thing company-wide.',
    },
    {
      icon: 'M12 3a9 9 0 1 0 0 18a9 9 0 1 0 0 -18M3 12h18M12 3c3 3 3 15 0 18M12 3c-3 3 -3 15 0 18',
      title: 'Global Collaboration',
      desc: 'Multilingual workspaces and cross-timezone comments let teams in different cities share one data view.',
    },
    {
      icon: 'M12 3a9 9 0 1 0 0 18a9 9 0 1 0 0 -18M12 7v5l3 2',
      title: 'Smart Alerts',
      desc: 'Anomaly spikes auto-push to Feishu and email with likely causes and related dashboards — issues never sleep.',
    },
  ];

  protected readonly bars: ReadonlyArray<number> = [38, 55, 44, 72, 62, 88, 76, 96, 84, 100];

  protected readonly chips: ReadonlyArray<Chip> = [
    { label: 'Daily Active Dashboards', value: '328' },
    { label: 'Automated Reports', value: '1,204' },
    { label: 'Data Sources', value: '64' },
  ];

  protected readonly metrics: ReadonlyArray<Metric> = [
    { value: '2,400+', label: 'Teams using it' },
    { value: '99.99%', label: 'Service availability' },
    { value: '0.8s', label: 'Median metric query time' },
    { value: '60+', label: 'Ready-to-use connectors' },
  ];

  protected toggleMenu(): void {
    this.menuOpen.update((open) => !open);
  }
}

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
    '蓝紫色抽象渐变光影，作为数据面板的背景',
  );

  protected readonly clients: ReadonlyArray<string> = [
    '北辰科技',
    '云栖资本',
    'Atlas',
    'Lumen Labs',
    '星河数据',
    'Nova Studio',
  ];

  protected readonly features: ReadonlyArray<Feature> = [
    {
      icon: 'M13 2l-1 7h6l-8 13l1 -9H5l8 -11z',
      title: '实时数据管道',
      desc: '秒级同步 60+ 业务数据源，无需维护定时脚本，让报表永远停在最新一刻。',
    },
    {
      icon: 'M12 3l8 3v6c0 5 -3.5 8 -8 9c-4.5 -1 -8 -4 -8 -9V6l8 -3zM9 12l2 2l4 -4',
      title: '企业级安全',
      desc: '字段级权限、全链路审计与 SSO 单点登录，通过 SOC 2 Type II 与等保三级认证。',
    },
    {
      icon: 'M4 4v16h16M8 15l3 -4l3 3l5 -7',
      title: '自助式分析',
      desc: '拖拽即可完成漏斗、留存与归因分析，业务同学不必再排队等数据团队排期。',
    },
    {
      icon: 'M12 3l9 5l-9 5l-9 -5l9 -5zM3 13l9 5l9 -5M3 17l9 5l9 -5',
      title: '统一指标层',
      desc: '把散落在各系统的口径收敛成一套指标定义，同一个数字，全公司只有一个答案。',
    },
    {
      icon: 'M12 3a9 9 0 1 0 0 18a9 9 0 1 0 0 -18M3 12h18M12 3c3 3 3 15 0 18M12 3c-3 3 -3 15 0 18',
      title: '全球协作',
      desc: '多语言工作区与跨时区评论，让分布在不同城市的团队共享同一张数据视图。',
    },
    {
      icon: 'M12 3a9 9 0 1 0 0 18a9 9 0 1 0 0 -18M12 7v5l3 2',
      title: '智能预警',
      desc: '异常波动自动推送到飞书与邮件，附带可能原因与相关看板，问题不过夜。',
    },
  ];

  protected readonly bars: ReadonlyArray<number> = [38, 55, 44, 72, 62, 88, 76, 96, 84, 100];

  protected readonly chips: ReadonlyArray<Chip> = [
    { label: '日活看板', value: '328' },
    { label: '自动报告', value: '1,204' },
    { label: '数据源', value: '64' },
  ];

  protected readonly metrics: ReadonlyArray<Metric> = [
    { value: '2,400+', label: '正在使用的团队' },
    { value: '99.99%', label: '服务可用性' },
    { value: '0.8s', label: '指标查询中位耗时' },
    { value: '60+', label: '开箱即用连接器' },
  ];

  protected toggleMenu(): void {
    this.menuOpen.update((open) => !open);
  }
}

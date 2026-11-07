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
    { label: '受托资产规模', value: '¥ 1,860 亿', note: '较上年 +12.4%' },
    { label: '服务企业客户', value: '420+', note: '覆盖 18 个行业' },
    { label: '平均合作年限', value: '8.6 年', note: '长期陪伴' },
  ];

  protected readonly services: ReadonlyArray<Service> = [
    {
      icon: 'M4 19h16M6 16V9m6 7V5m6 11v-4M4 19l0 1',
      title: '资产管理',
      desc: '以严谨的资产配置框架与全流程风控，为机构与家族客户管理长期资金。',
    },
    {
      icon: 'M3 10l9 -7l9 7M5 10v9h14v-9M9 19v-6h6v6',
      title: '企业融资',
      desc: '结合行业周期与企业阶段，设计可持续的融资结构与资本市场路径。',
    },
    {
      icon: 'M4 4h16v16H4zM8 14l3 -3l2 2l3 -4',
      title: '研究洞察',
      desc: '由 42 位注册分析师持续跟踪宏观与产业变化，输出可验证的判断。',
    },
  ];

  protected readonly metrics: ReadonlyArray<Metric> = [
    { value: '1998', label: '成立年份' },
    { value: '18', label: '覆盖行业' },
    { value: '42', label: '注册分析师' },
    { value: '99.2%', label: '客户续约率' },
  ];

  protected readonly insights: ReadonlyArray<Insight> = [
    { date: '2026.10.28', category: '宏观策略', title: '利率拐点之后：重新审视久期与现金流的平衡' },
    { date: '2026.10.15', category: '产业研究', title: '制造业资本开支周期：从设备更新到产能出海' },
    { date: '2026.09.30', category: '资产配置', title: '四季度展望：在不确定性中保持组合韧性' },
    { date: '2026.09.12', category: 'ESG 观察', title: '可持续披露标准化对估值体系的影响' },
  ];

  protected toggleMenu(): void {
    this.menuOpen.update((open) => !open);
  }
}

export interface ThemeMeta {
  readonly slug: 'saas' | 'commerce' | 'studio' | 'finance';
  readonly name: string;
  readonly tagline: string;
  readonly description: string;
  readonly route: string;
  readonly palette: readonly string[];
  readonly fontLabel: string;
  readonly traits: readonly string[];
}

export const THEMES: ReadonlyArray<ThemeMeta> = [
  {
    slug: 'saas',
    name: 'Aether',
    tagline: $localize`:@@themeSaasTagline:科技 SaaS`,
    description: $localize`:@@themeSaasDescription:以深海军蓝为底，融合青紫电光渐变与玻璃拟态面板，为数据驱动型产品打造的充满未来感的站点。`,
    route: '/examples/themes/saas',
    palette: ['#0B1020', '#22D3EE', '#7C5CFF', '#F3F6FF'],
    fontLabel: 'Space Grotesk / Inter',
    traits: [
      $localize`:@@themeSaasTrait1:深色模式`,
      $localize`:@@themeSaasTrait2:玻璃拟态`,
      $localize`:@@themeSaasTrait3:渐变光晕`,
    ],
  },
  {
    slug: 'commerce',
    name: 'Maison',
    tagline: $localize`:@@themeCommerceTagline:生活方式电商`,
    description: $localize`:@@themeCommerceDescription:温暖的米白与陶土色，衬线标题、纤细分隔线与充裕留白，宛如一本安静的生活杂志。`,
    route: '/examples/themes/commerce',
    palette: ['#FAF7F2', '#C4714B', '#5B6B4A', '#2A2620'],
    fontLabel: 'Playfair Display / Lora',
    traits: [
      $localize`:@@themeCommerceTrait1:杂志排版`,
      $localize`:@@themeCommerceTrait2:暖色留白`,
      $localize`:@@themeCommerceTrait3:轻盈雅致`,
    ],
  },
  {
    slug: 'studio',
    name: 'Atelier',
    tagline: $localize`:@@themeStudioTagline:创意作品集`,
    description: $localize`:@@themeStudioDescription:近黑底色、荧光柠檬黄与超大号压缩字重，借非对称网格传递大胆的创意态度。`,
    route: '/examples/themes/studio',
    palette: ['#111111', '#E8FF47', '#F5F5F0', '#3A3A3A'],
    fontLabel: 'Oswald / Helvetica Neue',
    traits: [
      $localize`:@@themeStudioTrait1:粗野主义字体`,
      $localize`:@@themeStudioTrait2:高对比`,
      $localize`:@@themeStudioTrait3:无圆角`,
    ],
  },
  {
    slug: 'finance',
    name: 'Meridian',
    tagline: $localize`:@@themeFinanceTagline:金融商务`,
    description: $localize`:@@themeFinanceDescription:深海军蓝与香槟金、对称网格与克制的阴影，传达稳重、专业与可信赖的气质。`,
    route: '/examples/themes/finance',
    palette: ['#0C2340', '#C9A961', '#F5F7FA', '#FFFFFF'],
    fontLabel: 'Lora / Roboto',
    traits: [
      $localize`:@@themeFinanceTrait1:对称网格`,
      $localize`:@@themeFinanceTrait2:纤细金线`,
      $localize`:@@themeFinanceTrait3:企业质感`,
    ],
  },
];

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
    tagline: '科技 SaaS',
    description: '深墨蓝底色、青紫电光渐变与玻璃质感面板，为数据型产品而生的未来感官网。',
    route: '/examples/themes/saas',
    palette: ['#0B1020', '#22D3EE', '#7C5CFF', '#F3F6FF'],
    fontLabel: 'Space Grotesk / Inter',
    traits: ['深色模式', '玻璃拟态', '渐变光晕'],
  },
  {
    slug: 'commerce',
    name: 'Maison',
    tagline: '生活方式电商',
    description: '暖米白与陶土色、衬线大标题、细线分隔和大量留白，像一本安静的生活杂志。',
    route: '/examples/themes/commerce',
    palette: ['#FAF7F2', '#C4714B', '#5B6B4A', '#2A2620'],
    fontLabel: 'Playfair Display / Lora',
    traits: ['编辑式排版', '暖色留白', '轻优雅'],
  },
  {
    slug: 'studio',
    name: 'Atelier',
    tagline: '创意作品集',
    description: '近黑背景、荧光柠檬与超大压缩字重，用不对称网格表达强烈的创作态度。',
    route: '/examples/themes/studio',
    palette: ['#111111', '#E8FF47', '#F5F5F0', '#3A3A3A'],
    fontLabel: 'Oswald / Helvetica Neue',
    traits: ['粗野排版', '高对比', '零圆角'],
  },
  {
    slug: 'finance',
    name: 'Meridian',
    tagline: '金融商务',
    description: '深海军蓝与香槟金、对称栅格与克制阴影，传递稳健、专业与可信赖感。',
    route: '/examples/themes/finance',
    palette: ['#0C2340', '#C9A961', '#F5F7FA', '#FFFFFF'],
    fontLabel: 'Lora / Roboto',
    traits: ['对称栅格', '细金线', '企业气质'],
  },
];

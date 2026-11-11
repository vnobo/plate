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
    tagline: 'Tech SaaS',
    description: 'Deep navy base, cyan-violet electric gradients and glassmorphism panels — a futuristic site built for data-driven products.',
    route: '/examples/themes/saas',
    palette: ['#0B1020', '#22D3EE', '#7C5CFF', '#F3F6FF'],
    fontLabel: 'Space Grotesk / Inter',
    traits: ['Dark Mode', 'Glassmorphism', 'Gradient Glow'],
  },
  {
    slug: 'commerce',
    name: 'Maison',
    tagline: 'Lifestyle Commerce',
    description: 'Warm off-white and terracotta, serif display headings, thin dividers and generous whitespace — a quiet lifestyle magazine.',
    route: '/examples/themes/commerce',
    palette: ['#FAF7F2', '#C4714B', '#5B6B4A', '#2A2620'],
    fontLabel: 'Playfair Display / Lora',
    traits: ['Editorial', 'Warm whitespace', 'Light elegance'],
  },
  {
    slug: 'studio',
    name: 'Atelier',
    tagline: 'Creative Portfolio',
    description: 'Near-black background, fluorescent lemon and oversized condensed weights express a bold creative attitude through asymmetric grids.',
    route: '/examples/themes/studio',
    palette: ['#111111', '#E8FF47', '#F5F5F0', '#3A3A3A'],
    fontLabel: 'Oswald / Helvetica Neue',
    traits: ['Brutalist type', 'High contrast', 'No rounding'],
  },
  {
    slug: 'finance',
    name: 'Meridian',
    tagline: 'Finance Business',
    description: 'Deep navy and champagne gold, symmetric grids and restrained shadows convey stability, professionalism and trust.',
    route: '/examples/themes/finance',
    palette: ['#0C2340', '#C9A961', '#F5F7FA', '#FFFFFF'],
    fontLabel: 'Lora / Roboto',
    traits: ['Symmetric grid', 'Thin gold lines', 'Corporate feel'],
  },
];

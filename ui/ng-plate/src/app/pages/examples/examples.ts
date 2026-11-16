import { Routes } from '@angular/router';

export const EXAMPLES_ROUTES: Routes = [
  {
    path: 'transfer',
    loadComponent: () => import('./transfer-demo').then((m) => m.TransferDemoComponent),
    title: $localize`:@@route.examples.transfer:穿梭框示例`,
  },
  {
    path: 'data-table',
    loadComponent: () => import('./data-table-demo').then((m) => m.DataTableDemoComponent),
    title: $localize`:@@route.examples.dataTable:数据表格示例`,
  },
  {
    path: 'themes',
    pathMatch: 'full',
    loadComponent: () => import('./themes/themes-gallery').then((m) => m.ThemesGallery),
    title: $localize`:@@route.examples.themes:主题展板`,
  },
  {
    path: 'themes/saas',
    loadComponent: () => import('./themes/saas-site').then((m) => m.SaasSite),
    title: $localize`:@@route.examples.saas:Aether · 科技 SaaS 主题`,
  },
  {
    path: 'themes/commerce',
    loadComponent: () => import('./themes/commerce-site').then((m) => m.CommerceSite),
    title: $localize`:@@route.examples.commerce:Maison · 生活方式电商主题`,
  },
  {
    path: 'themes/studio',
    loadComponent: () => import('./themes/studio-site').then((m) => m.StudioSite),
    title: $localize`:@@route.examples.studio:Atelier · 创意作品集主题`,
  },
  {
    path: 'themes/finance',
    loadComponent: () => import('./themes/finance-site').then((m) => m.FinanceSite),
    title: $localize`:@@route.examples.finance:Meridian · 金融商务主题`,
  },
  {
    path: '',
    redirectTo: 'transfer',
    pathMatch: 'full',
  },
];

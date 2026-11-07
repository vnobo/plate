import { Routes } from '@angular/router';

export const EXAMPLES_ROUTES: Routes = [
  {
    path: 'transfer',
    loadComponent: () => import('./transfer-demo').then((m) => m.TransferDemoComponent),
    data: { title: 'Transfer Component' },
  },
  {
    path: 'data-table',
    loadComponent: () => import('./data-table-demo').then((m) => m.DataTableDemoComponent),
    data: { title: 'Data Table Component' },
  },
  {
    path: 'themes',
    pathMatch: 'full',
    loadComponent: () => import('./themes/themes-gallery').then((m) => m.ThemesGallery),
    title: '主题方案展台',
  },
  {
    path: 'themes/saas',
    loadComponent: () => import('./themes/saas-site').then((m) => m.SaasSite),
    title: 'Aether · 科技 SaaS 主题',
  },
  {
    path: 'themes/commerce',
    loadComponent: () => import('./themes/commerce-site').then((m) => m.CommerceSite),
    title: 'Maison · 生活方式电商主题',
  },
  {
    path: 'themes/studio',
    loadComponent: () => import('./themes/studio-site').then((m) => m.StudioSite),
    title: 'Atelier · 创意作品集主题',
  },
  {
    path: 'themes/finance',
    loadComponent: () => import('./themes/finance-site').then((m) => m.FinanceSite),
    title: 'Meridian · 金融商务主题',
  },
  {
    path: '',
    redirectTo: 'transfer',
    pathMatch: 'full',
  },
];

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
    title: 'Theme Showcase',
  },
  {
    path: 'themes/saas',
    loadComponent: () => import('./themes/saas-site').then((m) => m.SaasSite),
    title: 'Aether · Tech SaaS Theme',
  },
  {
    path: 'themes/commerce',
    loadComponent: () => import('./themes/commerce-site').then((m) => m.CommerceSite),
    title: 'Maison · Lifestyle Commerce Theme',
  },
  {
    path: 'themes/studio',
    loadComponent: () => import('./themes/studio-site').then((m) => m.StudioSite),
    title: 'Atelier · Creative Portfolio Theme',
  },
  {
    path: 'themes/finance',
    loadComponent: () => import('./themes/finance-site').then((m) => m.FinanceSite),
    title: 'Meridian · Finance Business Theme',
  },
  {
    path: '',
    redirectTo: 'transfer',
    pathMatch: 'full',
  },
];

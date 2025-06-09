import { RenderMode, ServerRoute } from '@angular/ssr';

export const serverRoutes: ServerRoute[] = [
  {
    path: '',
    renderMode: RenderMode.Client,
  },
  {
    path: 'exception/403',
    renderMode: RenderMode.Prerender,
  },
  {
    path: 'exception/404',
    renderMode: RenderMode.Prerender,
  },
  {
    path: 'exception/500',
    renderMode: RenderMode.Prerender,
  },
  {
    path: 'exception/trigger',
    renderMode: RenderMode.Prerender,
  },
  {
    path: 'passport/login', // This page requires user-specific data, so we use SSR
    renderMode: RenderMode.Server,
  },
  {
    path: '**',
    renderMode: RenderMode.Prerender,
  },
];

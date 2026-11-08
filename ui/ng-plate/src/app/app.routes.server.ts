import { RenderMode, ServerRoute } from '@angular/ssr';

// All routes are intentionally client-only: this is an authenticated B2B admin
// console with no SEO requirement. The SSR infrastructure stays enabled (see
// angular.json server/ssr config and the serve:ssr:ng-plate script) so the app
// remains SSR-ready, while the server returns only the CSR shell.
export const serverRoutes: ServerRoute[] = [
  {
    path: 'passport/login',
    renderMode: RenderMode.Client,
  },
  {
    path: '**',
    renderMode: RenderMode.Client,
  },
];

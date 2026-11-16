import {
  AngularNodeAppEngine,
  createNodeRequestHandler,
  isMainModule,
  writeResponseToNodeResponse,
} from '@angular/ssr/node';
import express from 'express';
import { join } from 'node:path';

import {
  DEFAULT_LOCALE,
  localeFromAcceptLanguage,
  localeFromCookie,
  type LocaleId,
} from './app/core/i18n/locale';

const browserDistFolder = join(import.meta.dirname, '../browser');

const app = express();
const angularApp = new AngularNodeAppEngine();

/** Hashed build output can be cached forever; HTML, manifests and workers must revalidate. */
const CACHEABLE_ASSET = /\.(?:js|css|woff2?|ttf|otf|png|jpe?g|gif|svg|webp|avif|ico|cur)$/i;
const SERVICE_WORKER = /(?:ngsw-worker|safety-worker|worker-basic(?:\.min)?)\.js$/i;

/**
 * Serve static files from /browser. The zh-Hans (source locale) build lives at the root
 * and the en-US build in the /en-US subtree, so `/en-US/<asset>` resolves correctly here.
 */
app.use(
  express.static(browserDistFolder, {
    maxAge: '1y',
    immutable: true,
    index: false,
    redirect: false,
    setHeaders: (res, filePath) => {
      if (SERVICE_WORKER.test(filePath) || !CACHEABLE_ASSET.test(filePath)) {
        res.setHeader('Cache-Control', 'no-cache');
      }
    },
  }),
);

/**
 * Responses rendered by the app engine are locale-specific shells. Revalidate them instead
 * of letting a shared cache keep them; the static handler above already sets the policy for
 * build output.
 */
app.use((_request, response, next) => {
  response.setHeader('Cache-Control', 'no-cache');
  next();
});

/**
 * `GET /` serves the source locale (zh-Hans). A language picked earlier is stored in the
 * `plate-lang` cookie, otherwise the browser's Accept-Language decides. Both languages are
 * localized builds of the same application, so only the en-US subPath needs a redirect.
 */
app.use((req, res, next) => {
  if ((req.method !== 'GET' && req.method !== 'HEAD') || req.path !== '/') {
    return next();
  }
  if (!req.accepts('html')) {
    return next();
  }

  res.setHeader('Vary', 'Cookie, Accept-Language');
  const preferred: LocaleId | null =
    localeFromCookie(req.headers.cookie) ?? localeFromAcceptLanguage(req.headers['accept-language']);
  if (preferred === null || preferred === DEFAULT_LOCALE) {
    return next();
  }

  return res.redirect(302, `/${preferred}/`);
});

/**
 * Every other request is rendered by the Angular app engine. Its build manifest lists both
 * locales (`supportedLocales`), so `/en-US/**` is served by the en-US entry point.
 */
app.use((req, res, next) => {
  angularApp
    .handle(req)
    .then((response) =>
      response ? writeResponseToNodeResponse(response, res) : next(),
    )
    .catch(next);
});

/**
 * Start the server if this module is the main entry point, or it is ran via PM2.
 * The server listens to the port defined by the `PORT` environment variable, or defaults to 4000.
 */
if (isMainModule(import.meta.url) || process.env['pm_id']) {
  const port = process.env['PORT'] || 4000;
  app.listen(port, (error) => {
    if (error) {
      throw error;
    }

    console.log(`Node Express server listening on http://localhost:${port}`);
  });
}

/**
 * Request handler used by the Angular CLI (for dev-server and during build) or Firebase Cloud Functions.
 */
export const reqHandler = createNodeRequestHandler(app);

import {Injectable} from '@angular/core';
import {HttpEvent, HttpHandler, HttpInterceptor, HttpRequest} from '@angular/common/http';
import {finalize, Observable} from 'rxjs';
import {ProgressBarService} from "../shared/progress-bar.service";

/**
 * This code is an interceptor that adds a header to the request and shows a progress bar while the request is being processed. It then sends the request to the next handler and hides the progress bar when the request is finished.
 *  Step-by-step explanation:
 * 1. The interceptor takes in two parameters, a HttpRequest and a HttpHandler.
 * 2. A new url is created by concatenating '/api' with the original request url.
 * 3. A clone of the request is created and the headers are set to include 'X-Requested-With', 'XMLHttpRequest'.
 * 4. The cloned request is sent to the next handler.
 * 5. The progress bar is shown while the request is being processed.
 * 6. When the request is finished, the progress bar is hidden.
 */
@Injectable()
export class XhrInterceptor implements HttpInterceptor {

  constructor(private progressBar: ProgressBarService) {
  }

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    this.progressBar.show();
    const originalUrl = '/api' + req.url;
    const xhrReq = req.clone({
      headers: req.headers.set('X-Requested-With', 'XMLHttpRequest'),
      url: originalUrl
    });
    return next.handle(xhrReq).pipe(finalize(() => this.progressBar.hide()));
  }

}

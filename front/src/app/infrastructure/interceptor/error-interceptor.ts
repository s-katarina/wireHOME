import { Injectable } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor,
  HttpResponse,
  HttpErrorResponse,
} from '@angular/common/http';
import { catchError, Observable, throwError } from 'rxjs';

@Injectable()
export class ErrorInterceptor implements HttpInterceptor {
  intercept(
    req: HttpRequest<any>,
    next: HttpHandler
  ): Observable<HttpEvent<any>> {
    
    return next.handle(req)
                .pipe(
                  catchError((error: HttpErrorResponse) => {
                    let message: string = "";
                    if (error.error instanceof ErrorEvent)
                      message = error.error.message;
                    else
                      message = `${error.status}: ${error.error.message}`;
                    alert(message);
                    return throwError(() => new Error(error.error.message));
                  })
                )

  }
}

import { Injectable } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor,
  HttpErrorResponse
} from '@angular/common/http';
import { Observable, catchError, throwError } from 'rxjs';
import { Router } from '@angular/router';
import { AuthService } from 'src/app/modules/auth/service/auth.service';
import Swal from 'sweetalert2';

@Injectable()
export class TokenExpirationInterceptor implements HttpInterceptor {

  constructor(private readonly authService: AuthService,
    private router: Router) {}
  intercept(
  req: HttpRequest<any>,
  next: HttpHandler
  ): Observable<HttpEvent<any>> {

  return next.handle(req)
            .pipe(
              catchError((error: HttpErrorResponse) => {
                console.log("nananannananan")
                if (error instanceof HttpErrorResponse && error.status === 403) {
                    console.log("nananannananana")
                    return this.handle403Error(req, next);
                }
                return throwError(() => new Error(error.message));
              })
            )

}
private handle403Error(request: HttpRequest<any>, next: HttpHandler) {

        this.authService.logout()
        this.router.navigate(["/login"])
        this.fireSwalToast("Oh no, token expired. Please log in again.");
        return throwError(() => new Error("error.message"));
      
}

private fireSwalToast(title: string): void {
    const Toast = Swal.mixin({
      toast: true,
      position: 'top-end',
      showConfirmButton: false,
      timer: 3000,
      timerProgressBar: true,
      didOpen: (toast) => {
        toast.addEventListener('mouseenter', Swal.stopTimer)
        toast.addEventListener('mouseleave', Swal.resumeTimer)
      }
    })
    
    Toast.fire({
      icon: 'error',
      title: title
    })
  }
}

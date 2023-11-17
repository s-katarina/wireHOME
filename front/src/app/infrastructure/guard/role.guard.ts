import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot, UrlTree } from '@angular/router';
import { JwtHelperService } from '@auth0/angular-jwt';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class RoleGuard implements CanActivate {

  constructor(private router: Router) {}

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot): Observable<boolean | UrlTree> | Promise<boolean | UrlTree> | boolean | UrlTree {
    
      let roles = route.data['roles'] as Array<string>
      const accessToken: any = localStorage.getItem('user')
      if (accessToken == null) {
        this.router.navigateByUrl('/')
        return false
      }
      const helper = new JwtHelperService();
      const role = helper.decodeToken(accessToken).role

      if (roles.includes(role))
        return true
      else {
        this.router.navigateByUrl('/')
        return false
      }

  }
  
}

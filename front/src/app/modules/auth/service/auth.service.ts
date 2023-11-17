import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { JwtHelperService } from '@auth0/angular-jwt';
import { BehaviorSubject, Observable } from 'rxjs';
import { AppUserDTO, LoginDTO, TokenResponseDTO } from 'src/app/model/model';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  user$ = new BehaviorSubject({});
  userState$ = this.user$.asObservable();

  constructor(private http: HttpClient) {
    this.user$.next({
      "email": this.getEmail(),
      "role": this.getRole(),
      "id": this.getId()
    });
  }

  login(loginDTO: LoginDTO): Observable<TokenResponseDTO> {
    return this.http.post<TokenResponseDTO>(environment.apiHost + "user/login", loginDTO);
  }

  register(appUserDTO: AppUserDTO): Observable<AppUserDTO> {
    return this.http.post<AppUserDTO>(environment.apiHost + "user", appUserDTO);
  }

  registerAdmin(appUserDTO: AppUserDTO): Observable<AppUserDTO> {
    return this.http.post<AppUserDTO>(environment.apiHost + "user/admin", appUserDTO);
  }

  uploadProfileImage(base64: string, userId: number): Observable<AppUserDTO> {
    return this.http.post<AppUserDTO>(environment.apiHost + "user/" + userId + "/profileImage", base64);
  }

  getProfileImage(userId: number): Observable<string> {
    return this.http.get(environment.apiHost + "user/" + userId + "/profileImage", {
      responseType: 'text'
    });
  }

  getRole(): any {
    if (this.isLoggedIn()) {
      const accessToken: any = localStorage.getItem('user');
      const helper = new JwtHelperService();
      const role = helper.decodeToken(accessToken).role;
      return role;
    }
    return null;
  }

  getEmail(): any {
    if (this.isLoggedIn()) {
      const accessToken: any = localStorage.getItem('user');
      const helper = new JwtHelperService();
      const email = helper.decodeToken(accessToken).sub;
      return email;
    }
    return null;
  }

  getId(): any {
    if (this.isLoggedIn()) {
      const accessToken: any = localStorage.getItem('user');
      const helper = new JwtHelperService();
      const id = helper.decodeToken(accessToken).id;
      return id;
    }
    return null;
  }

  isLoggedIn(): boolean {
    if (localStorage.getItem('user') != null) {
      return true;
    }
    return false;
  }

  setUser(): void {
    this.user$.next({
      "email": this.getEmail(),
      "role": this.getRole(),
      "id": this.getId()
    });
  }

  logout (): void {
    localStorage.removeItem("user")
    this.user$.next({})
  }
}

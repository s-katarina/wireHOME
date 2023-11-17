import { Component, OnInit } from '@angular/core';
import { AuthService } from '../../auth/service/auth.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-toolbar',
  templateUrl: './toolbar.component.html',
  styleUrls: ['./toolbar.component.css']
})
export class ToolbarComponent implements OnInit {

  user: any

  constructor(private authService: AuthService, private router: Router) { }

  ngOnInit(): void {
    this.authService.userState$.subscribe((res: any) => {
      this.user = res
      this.router.navigate(["/"])
    })
  }

  logout(): void {
    this.authService.logout()
    this.router.navigate(["/"])
  }

}

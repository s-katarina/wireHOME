import { Component, OnInit } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../service/auth.service';
import { LoginDTO, TokenResponseDTO } from 'src/app/model/model';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { AdminChangePasswordDialogComponent } from '../../layout/dialogs/admin-change-password-dialog/admin-change-password-dialog.component';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {

  constructor(private readonly router: Router, 
              private readonly authService: AuthService, 
              public dialog: MatDialog) { }

  loginForm = new FormGroup({
    email: new FormControl(),
    password: new FormControl()
  })
  
  errorMessage: string = ''

  ngOnInit(): void {
  }

  loginUser(): void {
    let loginDTO: LoginDTO = {
      email: this.loginForm.value.email,
      password: this.loginForm.value.password
    }

    this.authService.login(loginDTO).subscribe({
      next: (res: TokenResponseDTO) => {
        localStorage.setItem('user', JSON.stringify(res.accessToken))
        this.authService.setUser()
        this.errorMessage = ""
        console.log(this.authService.getId())
        console.log(this.authService.getEmail())
        console.log(this.authService.getRole())
        console.log(this.authService.getActive())

        let role: string = this.authService.getRole()
        let active: boolean = this.authService.getActive()

        if (role == "SUPER_ADMIN" && !active)
          this.dialog.open(AdminChangePasswordDialogComponent)
      },
      error: err => {
        console.error('Observable emitted an error: ' + err)
        this.errorMessage = "Wrong credentials"
      }
    })
  }

}

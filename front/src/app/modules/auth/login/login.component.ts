import { Component, OnInit } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../service/auth.service';
import { LoginDTO, TokenResponseDTO } from 'src/app/model/model';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {

  constructor(private readonly router: Router, private readonly authService: AuthService) { }

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
      },
      error: err => {
        console.error('Observable emitted an error: ' + err)
        this.errorMessage = "Wrong credentials"
      }
    })
  }

}

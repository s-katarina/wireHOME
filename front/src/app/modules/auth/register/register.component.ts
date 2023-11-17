import { Component, OnInit } from '@angular/core';
import { AuthService } from '../service/auth.service';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { AppUserDTO } from 'src/app/model/model';
import { Router } from '@angular/router';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
})
export class RegisterComponent implements OnInit {

  registerForm = new FormGroup({
    name: new FormControl('', Validators.required),
    lastname: new FormControl('', Validators.required),
    email: new FormControl('', [Validators.email, Validators.required]),
    password: new FormControl('', Validators.required),
  })

  errorMessage: string = ''
  fileToUpload: FormData = new FormData()
  toUpload: string = ''

  constructor (private authService: AuthService, private router: Router) { }

  ngOnInit(): void {
  }

  onFileSelected(event: any) {
    const selectedFile = event.target.files[0];

    let mb5size = 1024 * 1024 * 5
    if (selectedFile.size > mb5size) {
      this.errorMessage = "File is bigger than 5MB"
      let el = document.getElementById("imageFileInput") as HTMLInputElement
      el.value = ""
      return
    }

    const reader = new FileReader();
    reader.onload = (e: any) => {
      const base64String = btoa(e.target.result);
      this.toUpload = base64String;
    };
    reader.readAsBinaryString(selectedFile);
  }

  register(): void {
    console.log('Value of form: ' + JSON.stringify(this.registerForm.value))

    if (!this.registerForm.valid) {
      this.errorMessage = "Form is not valid."
      return
    }

    let appUserDTO: AppUserDTO = {
      id: null,
      name: this.registerForm.value.name!,
      lastName: this.registerForm.value.lastname!,
      email: this.registerForm.value.email!,
      password: this.registerForm.value.password!,
      active: null
    }

    let role: string = this.authService.getRole()
    let func: Observable<AppUserDTO> | null = null

    if (role == "SUPER_ADMIN") {
      appUserDTO.active = true
      func = this.authService.registerAdmin(appUserDTO)
    }
    else
      func = this.authService.register(appUserDTO)

    func!.subscribe({
      next: (res: AppUserDTO) => {
        console.log(res)

        let userId: number = res.id!

        this.authService.uploadProfileImage(this.toUpload, userId).subscribe({
          next: (res: AppUserDTO) => {
            console.log(res)
            this.router.navigate(["/"])
          },
          error: err => {
            console.error('Observable emitted an error: ' + err)
            this.errorMessage = "Profile image upload failed"
          }
        })
      },
      error: err => {
        console.error('Observable emitted an error: ' + err)
        this.errorMessage = "Registration failed"
      }
    })
  }

}

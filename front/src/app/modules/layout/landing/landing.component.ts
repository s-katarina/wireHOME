import { Component, OnInit } from '@angular/core';
import { AuthService } from '../../auth/service/auth.service';
import { LoginDTO, TokenResponseDTO } from 'src/app/model/model';

@Component({
  selector: 'app-landing',
  templateUrl: './landing.component.html',
  styleUrls: ['./landing.component.css']
})
export class LandingComponent implements OnInit {

  constructor(private authService: AuthService) { }

  // imagePath: string = ''

  ngOnInit(): void {
    // let userId: number = this.authService.getId()

    // this.authService.getProfileImage(userId).subscribe({
    //   next: (res: string) => {
    //     this.imagePath = "data:image/png;base64," + res
    //   },
    //   error: err => {
    //     console.error('Observable emitted an error: ' + err)
    //   }
    // })
  }

}

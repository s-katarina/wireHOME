import { Component, OnInit } from '@angular/core';
import { MatDialogRef } from '@angular/material/dialog';
import { AppUserDTO } from 'src/app/model/model';
import { AuthService } from 'src/app/modules/auth/service/auth.service';

@Component({
  selector: 'app-admin-change-password-dialog',
  templateUrl: './admin-change-password-dialog.component.html',
  styleUrls: ['./admin-change-password-dialog.component.css']
})
export class AdminChangePasswordDialogComponent implements OnInit {

  newPassword: string = ''

  constructor(public dialogRef: MatDialogRef<AdminChangePasswordDialogComponent>, 
    private authService: AuthService) { }

  ngOnInit(): void {
  }

  onCancelClick (): void {
    this.dialogRef.close()
  }

  onChangeClick (): void {
    this.authService.superadminChangePassword(this.newPassword).subscribe({
      next: (res: AppUserDTO) => {
        console.log(res)
        this.onCancelClick()
      },
      error: err => {
        console.error('Observable emitted an error: ' + err)
      }
    })
  }

  ngOnDestroy (): void {
    this.dialogRef.close()
  }

}

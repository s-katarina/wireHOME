import { Component, OnInit } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { PendingPropertyRequestDTO } from 'src/app/model/model';
import { PropertyServiceService } from '../service/property-service.service';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-property-requests',
  templateUrl: './property-requests.component.html',
  styleUrls: ['./property-requests.component.css']
})
export class PropertyRequestsComponent implements OnInit {

  constructor(private readonly propertyService: PropertyServiceService) { }

  requests : PendingPropertyRequestDTO[] = []

  private deleted$ = new BehaviorSubject<any>({});
  selectedValue$ = this.deleted$.asObservable();

  showRejectionPopup: boolean = false;
  rejectionReason: string = '';
  propertyForRejection: PendingPropertyRequestDTO | null = null;

  setValue(test: any) {
    this.deleted$.next(test);
  }

  ngOnInit(): void {
    this.propertyService.getPendingProperties().subscribe((res: any) => {
      this.requests = res
    })
  }

  onAcceptClick(request: PendingPropertyRequestDTO) {
    this.propertyService.acceptPending(request.id).subscribe((res: any) => {
      this.setValue("deleted")
        this.deleted$.subscribe((value) => {
            this.requests.forEach( (item, index) => {
              if (item === request) this.requests.splice(index, 1)
            })
          })
        this.fireSwalToast(true, "Successfully accepted.")
      }, (error) => {
      console.error('Error accepting:', error);
      this.fireSwalToast(false, "Oops. Something went wrong.")
    })
  }


  openRejectionPopup(request: PendingPropertyRequestDTO) {
    this.showRejectionPopup = true;
    this.propertyForRejection = request;
  }

  onSubmitClick() {
    this.propertyService.rejectPending(this.propertyForRejection!.id, this.rejectionReason).subscribe((res: any) => {
      this.setValue("deleted")
        this.deleted$.subscribe((value) => {
            this.requests.forEach( (item, index) => {
              if (item === this.propertyForRejection) this.requests.splice(index, 1)
            })
          })
      this.fireSwalToast(true, "Successfully rejected.")
    }, (error) => {
      console.error('Error rejecting:', error);
      this.fireSwalToast(false, "Oops. Something went wrong.")
    })
    this.showRejectionPopup = false;
  }

  onCancelClick() {
    this.showRejectionPopup = false;
    this.propertyForRejection = null;
    this.rejectionReason = '';
  }

  private fireSwalToast(success: boolean, title: string): void {
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
      icon: success ? 'success' : 'error',
      title: title
    })
  }

}

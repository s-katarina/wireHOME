import { AfterViewInit, Component, OnInit } from '@angular/core';
import { WebsocketService } from 'src/app/infrastructure/socket/websocket.service';
import { OutdoorDeviceService } from '../service/outdoor-device-service';
import { DeviceDTO, Sprinkler } from 'src/app/model/model';
import { FormGroup, FormControl } from '@angular/forms';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-sprinkler',
  templateUrl: './sprinkler.component.html',
  styleUrls: ['./sprinkler.component.css']
})
export class SprinklerComponent implements OnInit, AfterViewInit {

  constructor(private socketService: WebsocketService,
    private readonly sprinklerService: OutdoorDeviceService) {
      this.sprinklerService.selectedLampId.subscribe((res: string) => {
        this.sprinklerId = res;
        console.log("Sprinkler component constructed for sprinkler with id = " + this.sprinklerId)
      })
  }

  private sprinklerId: string = ""
  public sprinkler: Sprinkler | undefined;

  isButtonHovered: boolean = false;

  makeSchedule: boolean = false;

  startHour: number = 23;
  endHour: number = 7;
  days: Record<string, number> = {
    'Mon': 1,
    'Tue': 2,
    'Wen': 3,
    'Thu': 4,
    'Fri': 5,
    'Sat': 6,
    'Sun': 0
  }
  daysClicked: Record<string, boolean> = {
    'Mon': true,
    'Tue': true,
    'Wen': true,
    'Thu': true,
    'Fri': true,
    'Sat': true,
    'Sun': true
  }
  daysList: { key: string, value: number }[] = Object.entries(this.days).map(([key, value]) => ({ key, value }));


  ngOnInit(): void {
    this.sprinklerService.getSprinkler(this.sprinklerId).subscribe((res: any) => {
      console.log("Get sprinkler res:", res)
      this.sprinkler = res;
    })
  }

  ngAfterViewInit(): void {
    const stompClient: any = this.socketService.initWebSocket()

    stompClient.connect({}, () => {
      stompClient.subscribe(`/sprinkler/${this.sprinkler!.id}`, (message: { body: string }) => {
        try {
          const parsedData : Sprinkler = JSON.parse(message.body);
          console.log("From sprinkler socket:", parsedData)
          this.sprinkler = parsedData
          this.fireSwalToast(true, "Sprinkler updated")
          return this.sprinkler;
        } catch (error) {
          console.error('Error parsing JSON string:', error);
          return null;
        }
      })

      stompClient.subscribe(`/device/${this.sprinkler!.id}/state`, (message: { body: string }) => {
        console.log(message)
        if(message.body === "0") {
          this.sprinkler!.state = false;
          this.makeSchedule = false;
        } else if (message.body === "1") this.sprinkler!.state = true;
      })
    })
  }

  ngOnDestroy(): void {
    this.socketService.closeWebSocket();
  }

  onOnOffClick(): void {
    this.sprinklerService.putSprinklerOnOff(this.sprinkler!.id, !this.sprinkler!.on).subscribe((res: any) => {
      console.log("Result from put on off", res);
    });
  }

  onScheduleOnOffClick(): void {

  }

  onScheduleSaveClick(): void {
    let weekdays = Object.entries(this.daysClicked)
    .filter(([key, value]) => value === true)
    .map(([key, value]) => this.days[key]);
    console.log(this.startHour, this.endHour)
    console.log(weekdays)
    this.sprinklerService.putSprinklerSchedule(this.sprinklerId, this.startHour, this.endHour, weekdays).subscribe((res: any)=> {
      console.log(res);
    })
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

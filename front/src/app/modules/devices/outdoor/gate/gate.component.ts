import { AfterViewInit, Component, OnInit } from '@angular/core';
import { WebsocketService } from 'src/app/infrastructure/socket/websocket.service';
import { Gate } from 'src/app/model/model';
import Swal from 'sweetalert2';
import { OutdoorDeviceService } from '../service/outdoor-device-service';

@Component({
  selector: 'app-gate',
  templateUrl: './gate.component.html',
  styleUrls: ['./gate.component.css']
})
export class GateComponent implements OnInit, AfterViewInit  {
  

  constructor(private socketService: WebsocketService,
    private readonly gateService: OutdoorDeviceService) { 
    this.gateService.selectedLampId.subscribe((res: string) => {
    this.gateId = res;
})
}

  private gateId: string = ""
  public gate: Gate | undefined;
  public regime: string = "On"
  public open: string = "On"
  public online: string = "Online"
  public charging: string = "Battery"

  isButtonHovered: boolean = false;


  ngOnInit(): void {
    this.gateService.getGate(this.gateId).subscribe((res: any) => {
      this.gate = res;
      console.log(res)
      console.log(this.gate)
      this.regime = this.gate?.public ? "Public" : "Private"
      this.open = this.gate?.open ? "Open" : "Closed"
      this.online = this.gate?.state ? "Online" : "Offline"
      this.charging = this.gate?.usesElectricity ? "House" : "Autonom"
    })
  }

  ngAfterViewInit(): void {
    const stompClient: any = this.socketService.initWebSocket()

    stompClient.connect({}, () => {
      stompClient.subscribe(`/gate/${this.gate!.id}`, (message: { body: string }) => {
        console.log(message)
        try {
          const parsedData : Gate = JSON.parse(message.body);
          console.log(parsedData)
          this.gate = parsedData
          this.fireSwalToast(true, "Gate updated")
          this.regime = this.gate?.public ? "Public" : "Private"
          this.open = this.gate?.open ? "Open" : "Closed"
          this.online = this.gate?.state ? "Online" : "Offline"
          return this.gate;
        } catch (error) {
          console.error('Error parsing JSON string:', error);
          return null;
        }
      })
    })
  }

  onOffClick(): void {
    if (this.gate?.state) {
      this.gateService.postOff(this.gate.id).subscribe((res: any) => {
        console.log(res);
      });
    } else this.gateService.postOn(this.gate!.id).subscribe((res: any) => {
      console.log(res);
    });
  }

  
  onRegimeClick(): void {
    this.gateService.postRegimeOnOff(this.gate!.id, !this.gate?.public).subscribe((res: any) => {
      console.log(res);
    });
  }

  onOpenClick(): void {
      this.gateService.postOpenOnOff(this.gate!.id, !this.gate?.open).subscribe((res: any) => {
        console.log(res);
      });
    
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

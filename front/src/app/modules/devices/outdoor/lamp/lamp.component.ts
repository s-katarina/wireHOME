import { AfterViewInit, Component, OnInit } from '@angular/core';
import { WebsocketService } from 'src/app/infrastructure/socket/websocket.service';
import { Lamp } from 'src/app/model/model';
import Swal from 'sweetalert2';
import { OutdoorDeviceService } from '../service/outdoor-device-service';

@Component({
  selector: 'app-lamp',
  templateUrl: './lamp.component.html',
  styleUrls: ['./lamp.component.css']
})
export class LampComponent implements OnInit, AfterViewInit {

  constructor(private socketService: WebsocketService,
              private readonly lampService: OutdoorDeviceService) { 
      this.lampService.selectedLampId.subscribe((res: string) => {
        this.lampId = res;
        console.log("Lamp component constructed for lamp with id = " + this.lampId)
      })
  }

  private lampId: string = ""
  public lamp: Lamp | undefined;
  public automaticMode: string = "On"
  public bulbTurned: string = "On"
  public online: string = "Online"
  public charging: string = "Battery"

  isButtonHovered: boolean = false;


  ngOnInit(): void {
    this.lampService.getLamp(this.lampId).subscribe((res: any) => {
      this.lamp = res;
      this.automaticMode = this.lamp?.automatic ? "On" : "Off"
      this.bulbTurned = this.lamp?.bulbState ? "On" : "Off"
      this.online = this.lamp?.state ? "Online" : "Offline"
      this.charging = this.lamp?.usesElectricity ? "House/Autonom" : "Battery"
    })
  }

  ngAfterViewInit(): void {
    const stompClient: any = this.socketService.initWebSocket()

    stompClient.connect({}, () => {
      stompClient.subscribe('/lamp/8', (message: { body: string }) => {
        console.log(message)
        try {
          const parsedData : Lamp = JSON.parse(message.body);
          console.log(parsedData)
          this.lamp = parsedData
          this.fireSwalToast(true, "Lamp updated")
          this.automaticMode = this.lamp?.automatic ? "On" : "Off"
          this.bulbTurned = this.lamp?.bulbState ? "On" : "Off"
          this.online = this.lamp?.state ? "Online" : "Offline"
          return this.lamp;
        } catch (error) {
          console.error('Error parsing JSON string:', error);
          return null;
        }
      })
    })
  }

  onOffClick(): void {
    if (this.lamp?.state) {
      this.lampService.postOff(this.lamp.id).subscribe((res: any) => {
        console.log(res);
      });
    } else this.lampService.postOn(this.lamp!.id).subscribe((res: any) => {
      console.log(res);
    });
  }

  onBulbOnOffClick(): void {
    if (this.lamp?.bulbState) {
      this.lampService.postBulbOff(this.lamp.id).subscribe((res: any) => {
        console.log(res);
      });
    } else this.lampService.postBulbOn(this.lamp!.id).subscribe((res: any) => {
      console.log(res);
    });
  }

  onAutomaticOnOffClick(): void {
    if (this.lamp?.automatic) {
      this.lampService.postAutomaticOnOff(this.lamp.id, false).subscribe((res: any) => {
        console.log(res);
      });
    } else this.lampService.postAutomaticOnOff(this.lamp!.id, true).subscribe((res: any) => {
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

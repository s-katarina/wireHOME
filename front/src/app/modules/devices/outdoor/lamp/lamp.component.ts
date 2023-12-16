import { AfterViewInit, Component, OnDestroy, OnInit } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { WebsocketService } from 'src/app/infrastructure/socket/websocket.service';
import { ApiResponse, Lamp } from 'src/app/model/model';
import Swal from 'sweetalert2';
import { OutdoorDeviceService } from '../service/outdoor-device-service';
import { CanvasJS } from '@canvasjs/angular-charts';

@Component({
  selector: 'app-lamp',
  templateUrl: './lamp.component.html',
  styleUrls: ['./lamp.component.css']
})
export class LampComponent implements OnInit, AfterViewInit, OnDestroy {

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

  selectedOption: string = ""
  
  range = new FormGroup({
    start: new FormControl<Date | null>(null),
    end: new FormControl<Date | null>(null),
  });

  chart: any;

  ngOnInit(): void {
    this.chart = new CanvasJS.Chart("chartContainer", 
    {
      zoomEnabled: true,
      exportEnabled: true,
      theme: "light2",
      title: {
      text: "Light sensor values"
      },
      data: [{
      type: "line",
      xValueType: "dateTime",
      dataPoints: []
      }]
    })
    this.chart.render();
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
      stompClient.subscribe(`/lamp/${this.lamp!.id}`, (message: { body: string }) => {
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

      stompClient.subscribe(`/device/${this.lamp!.id}/state`, (message: { body: string }) => {
        console.log(message)
        if(message.body === "0") this.lamp!.state = false;
        else if (message.body === "1") this.lamp!.state = true;
        this.online = this.lamp?.state ? "Online" : "Offline"
      })
    })
  }

  ngOnDestroy(): void {
    this.socketService.closeWebSocket();
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

  dateFrom = ""
  dateTo = ""

  // Updates dateFrom and dateTo
  onDropdownChange() {
    let dateBefore = new Date().getTime()
    const currentDate = new Date();
    console.log('Selected Value:', this.selectedOption);
    if (this.selectedOption.includes("d")) {
      const stringWithoutD = this.selectedOption.replace('d', '');
      const resultNumber = Number(stringWithoutD);
      dateBefore = (new Date()).setDate(currentDate.getDate() - resultNumber);
    } else if (this.selectedOption.includes("h")) {
      const stringWithoutD = this.selectedOption.replace('h', '');
      const resultNumber = Number(stringWithoutD);
      const currentDate = new Date();
      dateBefore = (new Date()).setHours(currentDate.getHours() - resultNumber);
    }
    this.dateFrom = (Math.floor(dateBefore)).toString();
    this.dateTo = (Math.floor(currentDate.getTime())).toString();
    console.log('Date Range:', this.dateFrom, this.dateTo);
  }

  onSubmit() {
    if (this.dateFrom === "" || this.dateTo === "") return
    if (this.selectedOption == "range") {
      if ((this.range.value.start != null && this.range.value.start != null) 
        && this.range.controls.start.valid && this.range.controls.end.valid) {
          this.dateFrom = (this.range.value.start!.getTime()).toString();
          this.dateTo = (this.range.value.end!.getTime()).toString();
        }
    }

    this.lampService.getRangeLightSensor(this.lamp!.id, this.dateFrom, this.dateTo).subscribe((res: ApiResponse) => {
      if (res.status == 200) {
        console.log(res.data)
        const dataPoints = res.data.map((item: { timestamp: string; value: string; }) => ({
          x: parseInt(item.timestamp),
          y: parseFloat(item.value)
        }));
        console.log(dataPoints)
        this.chart.options.data[0].dataPoints = dataPoints;
        this.chart.render();
      }
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

import { Component, OnInit } from '@angular/core';
import { LargeEnergyService } from '../large-energy.service';
import { Router } from '@angular/router';
import { FormBuilder, FormControl, FormGroup } from '@angular/forms';
import { DatePipe } from '@angular/common';
import { CanvasJS } from '@canvasjs/angular-charts';
import { WebsocketService } from 'src/app/infrastructure/socket/websocket.service';
import Swal from 'sweetalert2';
import { SolarPanel } from 'src/app/model/model';

@Component({
  selector: 'app-solar-panel',
  templateUrl: './solar-panel.component.html',
  styleUrls: ['./solar-panel.component.css']
})
export class SolarPanelComponent implements OnInit {

  panelId: string = ""
  selectedOption: string = ""
  
  range = new FormGroup({
    start: new FormControl<Date | null>(null),
    end: new FormControl<Date | null>(null),
  });

  public panel: SolarPanel = {
    surfaceSize: 0,
    efficiency: 0,
    id: '',
    state: false,
    modelName: '',
    usesElectricity: false,
    imagePath: '',
    deviceType: '',
    consumptionAmount: 0,
    propertyId: 0,
    on: false
  };
  public surfaceSize: string = "On"
  public efficiency: string = "On"
  public online: string = "Online"
  public charging: string = "Battery"

  isButtonHovered: boolean = false;
  chart: any;
	
  constructor( private readonly largeEnergyDeviceService: LargeEnergyService,
    private socketService: WebsocketService,) { 
      this.largeEnergyDeviceService.selectedDeviceId$.subscribe((res: string) => {
        this.panelId = res;
        console.log(this.panelId)
      })


 }

  ngOnInit(): void {
    this.chart = new CanvasJS.Chart("chartContainer", 
    {
      zoomEnabled: true,
      exportEnabled: true,
      theme: "light2",
      title: {
      text: "Energy produced"
      },
      data: [{
      type: "line",
      xValueType: "dateTime",
      dataPoints: []
      }]
    })
    this.chart.render();
    this.largeEnergyDeviceService.getSolarPanel(this.panelId).subscribe((res: any) => {
      this.panel = res;
      this.surfaceSize = (this.panel?.surfaceSize || 0).toString()  
      const newLocal = this;
      newLocal.efficiency = (this.panel?.efficiency || 0).toString()
      this.online = this.panel?.state ? "Online" : "Offline"
      this.charging = this.panel?.usesElectricity ? "House/Autonom" : "Battery"
    })
  }
  ngAfterViewInit(): void {
    const stompClient: any = this.socketService.initWebSocket()

    stompClient.connect({}, () => {
      stompClient.subscribe(`/panel/${this.panel!.id}`, (message: { body: string }) => {
        console.log(message)
        try {
          const parsedData : SolarPanel = JSON.parse(message.body);
          console.log(parsedData)
          this.panel = parsedData
          this.fireSwalToast(true, "Lamp updated")
          this.surfaceSize = (this.panel?.surfaceSize || 0).toString()
          this.efficiency = (this.panel?.efficiency || 0).toString()
          this.online = this.panel?.state ? "Online" : "Offline"
          return this.panel;
        } catch (error) {
          console.error('Error parsing JSON string:', error);
          return null;
        }
      })
    })
  }

  onOffClick(): void {
    if (this.panel?.on) {
      this.largeEnergyDeviceService.postOff(this.panel.id).subscribe((res: any) => {
        console.log(res);
        this.panel.on = false
      });
    } else this.largeEnergyDeviceService.postOn(this.panel!.id).subscribe((res: any) => {
      console.log(res);
      this.panel.on = true
    });
  }

  // onBulbOnOffClick(): void {
  //   if (this.panel?.bulbState) {
  //     this.lampService.postBulbOff(this.panel.id).subscribe((res: any) => {
  //       console.log(res);
  //     });
  //   } else this.lampService.postBulbOn(this.panel!.id).subscribe((res: any) => {
  //     console.log(res);
  //   });
  // }

  // onAutomaticOnOffClick(): void {
  //   if (this.panel?.automatic) {
  //     this.lampService.postAutomaticOnOff(this.panel.id, false).subscribe((res: any) => {
  //       console.log(res);
  //     });
  //   } else this.lampService.postAutomaticOnOff(this.panel!.id, true).subscribe((res: any) => {
  //     console.log(res);
  //   });
  // }

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

  onSubmit() {
    const dateFrom = (this.range.value.start!.getTime()/1000).toString();
    const dateTo = (this.range.value.end!.getTime()/1000).toString();
    console.log('Date Range:', dateFrom, dateTo);
    //date range u milisekundama
    this.largeEnergyDeviceService.getSolarPlatformReadingFrom(this.panelId, dateFrom, dateTo, "energy-maintaining").subscribe((res: any) => {
      this.chart.options.data[0].dataPoints = res;
      console.log(res)
      this.chart.render();

    })
  }

  onDropdownChange(event: any) {
    // Handle the change event here
    // const selectedValue: string = event.target.value;
    console.log('Selected Value:', this.selectedOption);
    if (this.selectedOption == "range") return
    // this.selectedOption = selectedValue
    // Add your custom logic based on the selected value
    let dateBefore = new Date().getTime()
    let currentDate = new Date();
    if (this.selectedOption.includes("d")) {
      const stringWithoutD = this.selectedOption.replace('d', '');
      const resultNumber = Number(stringWithoutD);
      const currentDate = new Date();

      // Subtract 3 hours
      dateBefore = (new Date()).setDate(currentDate.getDate() - resultNumber);
      console.log("Current date:", currentDate);
      console.log("Date 7 days before:", dateBefore);

    }
    else if (this.selectedOption.includes("h")) {
      const stringWithoutD = this.selectedOption.replace('h', '');
      const resultNumber = Number(stringWithoutD);
      const currentDate = new Date();

      // Subtract 3 hours
      dateBefore = (new Date()).setHours(currentDate.getHours() - resultNumber);
    }

    const dateFrom = (Math.floor(dateBefore/1000)).toString();
    const dateTo = (Math.floor(currentDate.getTime()/1000)).toString();
    console.log('Date Range:', dateFrom, dateTo);
    //date range u milisekundama
    this.largeEnergyDeviceService.getSolarPlatformReadingFrom(this.panelId, dateFrom, dateTo, "energy-maintaining").subscribe((res: any) => {
      this.chart.options.data[0].dataPoints = res;
      console.log(res)
      this.chart.render();

    })
  }
}

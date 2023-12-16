import { Component, OnInit } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { WebsocketService } from 'src/app/infrastructure/socket/websocket.service';
import { Battery } from 'src/app/model/model';
import { LargeEnergyService } from '../large-energy.service';
import { CanvasJS } from '@canvasjs/angular-charts';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-battery',
  templateUrl: './battery.component.html',
  styleUrls: ['./battery.component.css']
})
export class BatteryComponent implements OnInit {

  batteryId: string = ""
  selectedOption: string = ""
  
  range = new FormGroup({
    start: new FormControl<Date | null>(null),
    end: new FormControl<Date | null>(null),
  });
  public battery: Battery = {
    capacity: 0,
    currentFill: 0,
    id: '',
    state: false,
    modelName: '',
    usesElectricity: false,
    imagePath: '',
    deviceType: '',
    consumptionAmount: 0,
    propertyId: 0,
    on: false
  }
  public capacity: string = "On"
  public currentFill: string = "On"
  public online: string = "Online"
  public charging: string = "Battery"

  isButtonHovered: boolean = false;
  chart: any;

  constructor(private readonly largeEnergyDeviceService: LargeEnergyService,
    private socketService: WebsocketService,) { 
      this.largeEnergyDeviceService.selectedDeviceId$.subscribe((res: string) => {
        this.batteryId = res;
        console.log(this.batteryId)
      })
    }

  ngOnInit(): void {
    this.chart = new CanvasJS.Chart("chartContainer", 
    {
      zoomEnabled: true,
      exportEnabled: true,
      theme: "light2",
      title: {
      text: "Battery state"
      },
      data: [{
      type: "line",
      xValueType: "dateTime",
      dataPoints: []
      }]
    })
    this.chart.render();
    this.largeEnergyDeviceService.getBattery(this.batteryId).subscribe((res: any) => {
      this.battery = res;
      this.capacity = (this.battery?.capacity || 0).toString()  
      const newLocal = this;
      newLocal.currentFill = (this.battery?.currentFill || 0).toString()
      this.online = this.battery?.state ? "Online" : "Offline"
      this.charging = this.battery?.usesElectricity ? "House/Autonom" : "Battery"
    })
  }

  ngAfterViewInit(): void {
    const stompClient: any = this.socketService.initWebSocket()

    stompClient.connect({}, () => {
      stompClient.subscribe(`/battery/${this.battery!.id}`, (message: { body: string }) => {
        console.log(message)
        try {
          const parsedData : Battery = JSON.parse(message.body);
          console.log(parsedData)
          this.battery = parsedData
          this.fireSwalToast(true, "Lamp updated")
          this.capacity = (this.battery?.capacity || 0).toString()
          this.currentFill = (this.battery?.currentFill || 0).toString()
          this.online = this.battery?.state ? "Online" : "Offline"
          return this.battery;
        } catch (error) {
          console.error('Error parsing JSON string:', error);
          return null;
        }
      })
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

  onSubmit() {
    const dateFrom = (this.range.value.start!.getTime()/1000).toString();
    const dateTo = (this.range.value.end!.getTime()/1000).toString();
    console.log('Date Range:', dateFrom, dateTo);
    //date range u milisekundama
    this.largeEnergyDeviceService.getSolarPlatformReadingFrom(this.batteryId, dateFrom, dateTo, "battery").subscribe((res: any) => {
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
    this.largeEnergyDeviceService.getSolarPlatformReadingFrom(this.batteryId, dateFrom, dateTo, "energy-maintaining").subscribe((res: any) => {
      this.chart.options.data[0].dataPoints = res;
      console.log(res)
      this.chart.render();

    })
  }

}

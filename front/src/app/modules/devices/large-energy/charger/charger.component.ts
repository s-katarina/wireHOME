import { Component, OnInit, ViewChild } from '@angular/core';
import { LargeEnergyService } from '../large-energy.service';
import { Router } from '@angular/router';
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { DatePipe } from '@angular/common';
import { CanvasJS } from '@canvasjs/angular-charts';
import { WebsocketService } from 'src/app/infrastructure/socket/websocket.service';
import Swal from 'sweetalert2';
import { ApiResponse, Car, Charger, GateEvent, SolarPanel } from 'src/app/model/model';
import { MatTable, MatTableDataSource } from '@angular/material/table';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';

@Component({
  selector: 'app-charger',
  templateUrl: './charger.component.html',
  styleUrls: ['./charger.component.css']
})
export class ChargerComponent implements OnInit {


  chargerId: string = ""
  selectedOption: string = ""

  displayedColumns : string[] = ['eventType', 'caller', 'callerUsername', 'timestamp']
  dataSource!: MatTableDataSource<GateEvent>;
  events : GateEvent[] = []
  recentEvents: GateEvent[] = []
  carsOnCharger: Car[] = []

  @ViewChild(MatPaginator) paginator! : MatPaginator;
  @ViewChild(MatSort) sort! : MatSort;
  @ViewChild(MatTable) eventTable!: MatTable<any>;

  public currentPage : number = 0;
  public pageSize : number = 10;
  public length : number = 0;

  public filterApplied : boolean = false;
  range2 = new FormGroup({
    start: new FormControl<Date | null>(null),
    end: new FormControl<Date | null>(null),
  });
  
  range = new FormGroup({
    start: new FormControl<Date | null>(null),
    end: new FormControl<Date | null>(null),
  });
  chargerForm: FormGroup;

  public charger: Charger = {
    chargingStrength: 0,
    portNumber: 0,
    availablePortNumber: 0,
    percentage: 0,
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
  public chargingStrength: string = "0"
  public portNumber: string = "0"
  public online: string = "Online"
  public availablePortNumber: string = "0"
  public percentage: number = 100
  chart:any

  isButtonHovered: boolean = false;
	
  constructor( private readonly largeEnergyDeviceService: LargeEnergyService,
    private socketService: WebsocketService,
    private fb: FormBuilder) { 
      this.largeEnergyDeviceService.selectedDeviceId$.subscribe((res: string) => {
        this.chargerId = res;

        console.log(this.chargerId)
      })
      this.chargerForm = this.fb.group({
        percentage: ['', [Validators.required, Validators.min(0), Validators.max(100)]]
      });
      // this.chargerId = '6';

 }

  ngOnInit(): void {
    
    this.largeEnergyDeviceService.getCharger(this.chargerId).subscribe((res: any) => {
      this.charger = res;
      this.percentage = this.charger.percentage
      this.chargingStrength = (this.charger?.chargingStrength || 0).toString()  
      const newLocal = this;
      newLocal.portNumber = (this.charger?.portNumber || 0).toString()
      this.online = this.charger?.state ? "Online" : "Offline"
      // this.availablePortNumber = this.charger?.usesElectricity ? "House/Autonom" : "Battery"
    })
    this.dataSource = new MatTableDataSource<GateEvent>(this.events);
    this.dataSource.paginator = this.paginator;
    this.largeEnergyDeviceService.getGateEvents(this.chargerId, "charger-event").subscribe((res: any) => {
      console.log(res)
      this.recentEvents = res.data
      this.events = this.recentEvents
      this.dataSource = new MatTableDataSource<GateEvent>(this.events);          
      this.eventTable.renderRows();   
      this.dataSource.sort = this.sort;
      this.dataSource.paginator = this.paginator;
      this.length = this.events.length;
    })

   
  }
  ngAfterViewInit(): void {
    const stompClient: any = this.socketService.initWebSocket()

    stompClient.connect({}, () => {
      stompClient.subscribe(`/charger/${this.charger!.id}`, (message: { body: string }) => {
        console.log(message)
        try {
          const parsedData : number = JSON.parse(message.body);
          console.log("parsed data", parsedData)
          this.charger.state = true
          this.charger.availablePortNumber = parsedData
        } catch (error) {
          console.error('Error parsing JSON string:', error);
        }
      })
      stompClient.subscribe(`/charger/${this.charger!.id}/car`, (message: { body: string }) => {
        console.log(message)
        try {
          this.charger.state = true
          this.online = this.charger?.state ? "Online" : "Offline"

          const newCar : Car = JSON.parse(message.body);
          console.log("parsed data", newCar)
          const existingCarIndex = this.carsOnCharger.findIndex(car => car.plate === newCar.plate);

          if (existingCarIndex !== -1) {
            // Car with the same license plate already exists, update its values
            this.carsOnCharger[existingCarIndex] = { ...this.carsOnCharger[existingCarIndex], ...newCar };
          } else {
            // Car with the given license plate is not in the list, add it
            this.carsOnCharger.push(newCar);
          }
          this.carsOnCharger = this.carsOnCharger.filter(car => car.percentage + 0.5 <= this.charger.percentage);
          console.log("cars", this.carsOnCharger)

        } catch (error) {
          console.error('Error parsing JSON string:', error);
        }
      })

      stompClient.subscribe(`/charger/${this.charger!.id}/event`, (message: { body: string }) => {
        console.log(message)
        try {
          const parsedData : GateEvent = JSON.parse(message.body);
          console.log(parsedData)
          this.recentEvents.push(parsedData)
          this.events = this.recentEvents
          const end = (this.currentPage + 1) * this.pageSize;
          const start = this.currentPage * this.pageSize;
          const part = this.events.slice(start, end);
          // Stop table rerendering if filter is applied
          if (!this.filterApplied) {
            this.dataSource = new MatTableDataSource<GateEvent>(part);          
            this.eventTable.renderRows();   
            this.dataSource.sort = this.sort;
            this.dataSource.paginator = this.paginator;
            this.length = this.events.length;
          } 
          return ""
        } catch (error) {
          console.error('Error parsing JSON string:', error);
          return null;
        }
      })
    })
  }

  ngOnDestroy(): void {
    // Close the socket connection when the component is destroyed
    this.socketService.closeWebSocket();
  }


  onOffClick(): void {
    if (this.charger?.on) {
      this.largeEnergyDeviceService.postOff(this.charger.id).subscribe((res: any) => {
        console.log(res);
        this.charger.on = false
      });
    } else this.largeEnergyDeviceService.postOn(this.charger!.id).subscribe((res: any) => {
      console.log(res);
      this.charger.on = true
    });
  }

  
  public handlePage(event?:any) {
    this.currentPage = event.pageIndex;
    this.pageSize = event.pageSize;
    this.pageIteration();
    this.dataSource.sort = this.sort;
  }
  
  private pageIteration() {
    const end = (this.currentPage + 1) * this.pageSize;
    const start = this.currentPage * this.pageSize;
    const part = this.events.slice(start, end);
    this.dataSource = new MatTableDataSource<GateEvent>(part);

    this.dataSource.data = part;
    this.length = this.events.length;
  }

  displayCaller(caller: string): string {
    switch (caller) {
      case "GATE_EVENT":
        return "Gate"
      case "USER":
        return "User"
      default: return caller
    }
  }

  displayTimestamp(timestamp: string): string {
    const unixTimestamp = parseInt(timestamp, 10);
  
    if (isNaN(unixTimestamp)) {
      return 'Invalid timestamp';
    }
  
    const date = new Date(unixTimestamp);
    const options: Intl.DateTimeFormatOptions = {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
      hour12: false,
  };
    return new Intl.DateTimeFormat('en-GB', options).format(date);
  }

  filterInitiator: string = '';
  filterEvent: string = '';

  applyFilter(): void {
    this.filterApplied = true;

    let filteredEvents: GateEvent[] = [];

    // Date range filter
    if ((this.range2.value.start != null && this.range2.value.start != null) 
        && this.range2.controls.start.valid && this.range2.controls.end.valid) { 
        this.largeEnergyDeviceService.getRangeGateEvents(this.charger!.id, Math.floor(this.range2.value.start!.getTime()).toString(), Math.floor(this.range2.value.end!.getTime()).toString(), "charger-event").subscribe((res: ApiResponse) => {
          if (res.status == 200) {
            console.log(res.data)
            filteredEvents = res.data.filter((event: { caller: string; eventType: string; }) =>
              event.caller.toLowerCase().includes(this.filterInitiator.toLowerCase()) &&
              event.eventType.toLowerCase().includes(this.filterEvent.toLowerCase())
              );
            this.events = filteredEvents
            this.dataSource = new MatTableDataSource<GateEvent>(this.events);
            console.log(this.events)
            this.dataSource.sort = this.sort;
            this.dataSource.paginator = this.paginator;
            this.length = this.events.length;
          }
        });
    } else {
      // Initiator and event type filter
      filteredEvents = this.recentEvents.filter(event =>
        event.caller.toLowerCase().includes(this.filterInitiator.toLowerCase()) &&
        event.eventType.toLowerCase().includes(this.filterEvent.toLowerCase())
      );
      this.events = filteredEvents
      this.dataSource = new MatTableDataSource<GateEvent>(this.events);
      this.dataSource.sort = this.sort;
      this.dataSource.paginator = this.paginator;
      this.length = this.events.length;
    }
    
  }

  clearFilter(): void {
    this.filterApplied = false;
    this.filterInitiator = '';
    this.range2.reset();
    this.events = this.recentEvents
    this.dataSource = new MatTableDataSource<GateEvent>(this.events);
    this.dataSource.sort = this.sort;
    this.dataSource.paginator = this.paginator;
    this.length = this.events.length;
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
    this.largeEnergyDeviceService.getSolarPlatformReadingFrom(this.chargerId, dateFrom, dateTo, "energy-maintaining").subscribe((res: any) => {
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
    this.largeEnergyDeviceService.getSolarPlatformReadingFrom(this.chargerId, dateFrom, dateTo, "energy-maintaining").subscribe((res: any) => {
      this.chart.options.data[0].dataPoints = res;
      console.log(res)
      console.log("hahahahahahahahahahaaaaaaaaa")
      this.chart.render();

    })
  }

  chagePercentage() {
    this.largeEnergyDeviceService.changePort(this.charger!.id, this.percentage).subscribe((res: Charger) => {
      this.charger = res;
      this.fireSwalToast(true, "Successfully added!")
    }, (error) => {
      console.error('Error', error);
      this.fireSwalToast(false, "Oops. Something went wrong.")
    })
  }
}

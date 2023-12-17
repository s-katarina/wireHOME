import { Component, OnInit, ViewChild } from '@angular/core';
import { LargeEnergyService } from '../large-energy.service';
import { Router } from '@angular/router';
import { FormBuilder, FormControl, FormGroup } from '@angular/forms';
import { DatePipe } from '@angular/common';
import { CanvasJS } from '@canvasjs/angular-charts';
import { WebsocketService } from 'src/app/infrastructure/socket/websocket.service';
import Swal from 'sweetalert2';
import { ApiResponse, GateEvent, SolarPanel } from 'src/app/model/model';
import { MatTable, MatTableDataSource } from '@angular/material/table';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';

@Component({
  selector: 'app-solar-panel',
  templateUrl: './solar-panel.component.html',
  styleUrls: ['./solar-panel.component.css']
})
export class SolarPanelComponent implements OnInit {

  panelId: string = ""
  selectedOption: string = ""

  displayedColumns : string[] = ['eventType', 'caller', 'timestamp'];
  dataSource!: MatTableDataSource<GateEvent>;
  events : GateEvent[] = [];
  recentEvents: GateEvent[] = [];

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
    this.dataSource = new MatTableDataSource<GateEvent>(this.events);
    this.dataSource.paginator = this.paginator;
    this.largeEnergyDeviceService.getGateEvents(this.panelId).subscribe((res: any) => {
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
      stompClient.subscribe(`/panel/${this.panel!.id}`, (message: { body: string }) => {
        console.log(message)
        try {
          const parsedData : SolarPanel = JSON.parse(message.body);
          console.log(parsedData)
          this.panel = parsedData
          this.fireSwalToast(true, "Lamp updated")
          this.online = this.panel?.state ? "Online" : "Offline"
          return this.panel;
        } catch (error) {
          console.error('Error parsing JSON string:', error);
          return null;
        }
      })

      stompClient.subscribe(`/gate/${this.panel!.id}/event`, (message: { body: string }) => {
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
  filterEvent: string = 'on/off';

  applyFilter(): void {
    this.filterApplied = true;

    let filteredEvents: GateEvent[] = [];

    // Date range filter
    if ((this.range2.value.start != null && this.range2.value.start != null) 
        && this.range2.controls.start.valid && this.range2.controls.end.valid) { 
        this.largeEnergyDeviceService.getRangeGateEvents(this.panel!.id, Math.floor(this.range2.value.start!.getTime()).toString(), Math.floor(this.range2.value.end!.getTime()).toString()).subscribe((res: ApiResponse) => {
          if (res.status == 200) {
            console.log(res.data)
            filteredEvents = res.data.filter((event: { caller: string; eventType: string; }) =>
            ((event.caller?.toLowerCase()) || "").includes(this.filterInitiator.toLowerCase()));
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
    this.filterEvent = '';
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
      console.log("hahahahahahahahahahaaaaaaaaa")
      this.chart.render();

    })
  }
}

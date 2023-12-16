import { AfterViewInit, Component, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { MatTable, MatTableDataSource } from '@angular/material/table';
import { WebsocketService } from 'src/app/infrastructure/socket/websocket.service';
import { ApiResponse, Gate, GateEvent } from 'src/app/model/model';
import Swal from 'sweetalert2';
import { OutdoorDeviceService } from '../service/outdoor-device-service';
import { DateAdapter } from '@angular/material/core';

@Component({
  selector: 'app-gate',
  templateUrl: './gate.component.html',
  styleUrls: ['./gate.component.css']
})
export class GateComponent implements OnInit, AfterViewInit, OnDestroy {
  

  constructor(private socketService: WebsocketService,
    private readonly gateService: OutdoorDeviceService,
    private dateAdapter: DateAdapter<Date>) { 
    this.gateService.selectedLampId.subscribe((res: string) => {
    this.gateId = res;
    this.dateAdapter.setLocale('en-GB'); //dd/MM/yyyy
})
}

  private gateId: string = ""
  public gate: Gate | undefined;
  public regime: string = "On"
  public open: string = "On"
  public online: string = "Online"
  public charging: string = "Battery"

  // isButtonHovered: boolean = false;

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

  range = new FormGroup({
    start: new FormControl<Date | null>(null),
    end: new FormControl<Date | null>(null),
  });



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
    this.dataSource = new MatTableDataSource<GateEvent>(this.events);
    this.dataSource.paginator = this.paginator;
    this.gateService.getGateEvents(this.gateId).subscribe((res: any) => {
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
    console.log(this.paginator); 

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

      stompClient.subscribe(`/gate/${this.gate!.id}/event`, (message: { body: string }) => {
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

      stompClient.subscribe(`/device/${this.gate!.id}/state`, (message: { body: string }) => {
        console.log(message)
        if(message.body === "0") this.gate!.state = false;
        else if (message.body === "1") this.gate!.state = true;
        this.online = this.gate?.state ? "Online" : "Offline"
      })
    })
  }

  ngOnDestroy(): void {
    // Close the socket connection when the component is destroyed
    this.socketService.closeWebSocket();
  }
  

  // onOffClick(): void {
  //   if (this.gate?.state) {
  //     this.gateService.postOff(this.gate.id).subscribe((res: any) => {
  //       console.log(res);
  //     });
  //   } else this.gateService.postOn(this.gate!.id).subscribe((res: any) => {
  //     console.log(res);
  //   });
  // }

  
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
    if ((this.range.value.start != null && this.range.value.start != null) 
        && this.range.controls.start.valid && this.range.controls.end.valid) { 
        this.gateService.getRangeGateEvents(this.gate!.id, Math.floor(this.range.value.start!.getTime()).toString(), Math.floor(this.range.value.end!.getTime()).toString()).subscribe((res: ApiResponse) => {
          if (res.status == 200) {
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
    this.filterEvent = '';
    this.range.reset();
    this.events = this.recentEvents
    this.dataSource = new MatTableDataSource<GateEvent>(this.events);
    this.dataSource.sort = this.sort;
    this.dataSource.paginator = this.paginator;
    this.length = this.events.length;
  }

  // filterByDateRange(events: GateEvent[]): GateEvent[] {

  //   const startDate = Math.floor(this.range.value.start!.getTime());;
  //   const endDate = Math.floor(this.range.value.end!.getTime());;

  //   return events.filter(event => {
  //     const eventDate = parseInt(event.timestamp, 10);
  //     return eventDate >= startDate && eventDate <= endDate;
  //   });
  // }

  licencePlate: string = '';

  public addLicencePlate() {
    this.gateService.putLicencePlate(this.gate!.id, this.licencePlate).subscribe((res: Gate) => {
      this.gate = res;
      this.fireSwalToast(true, "Successfully added!")
    }, (error) => {
      console.error('Error', error);
      this.fireSwalToast(false, "Oops. Something went wrong.")
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

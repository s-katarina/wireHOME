import { AfterViewInit, Component, OnInit, ViewChild } from '@angular/core';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { MatTable, MatTableDataSource } from '@angular/material/table';
import { WebsocketService } from 'src/app/infrastructure/socket/websocket.service';
import { Gate, GateEvent } from 'src/app/model/model';
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

  displayedColumns : string[] = ['eventType', 'caller', 'timestamp'];
  dataSource!: MatTableDataSource<GateEvent>;
  events : GateEvent[] = [];

  @ViewChild(MatPaginator) paginator! : MatPaginator;
  @ViewChild(MatSort) sort! : MatSort;
  @ViewChild(MatTable) eventTable!: MatTable<any>;

  public currentPage : number = 0;
  public pageSize : number = 10;
  public length : number = 0;


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
          this.events.push(parsedData)
          const end = (this.currentPage + 1) * this.pageSize;
          const start = this.currentPage * this.pageSize;
          const part = this.events.slice(start, end);
          this.dataSource = new MatTableDataSource<GateEvent>(part);          this.eventTable.renderRows();   
          this.dataSource.sort = this.sort;
          this.dataSource.paginator = this.paginator;
          this.length = this.events.length;
          return ""
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
    return new Intl.DateTimeFormat('en-US', options).format(date);
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

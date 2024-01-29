import { AfterViewInit, Component, OnInit, ViewChild } from '@angular/core';
import { WebsocketService } from 'src/app/infrastructure/socket/websocket.service';
import { OutdoorDeviceService } from '../service/outdoor-device-service';
import { ApiResponse, DeviceDTO, Sprinkler, SprinklerCommand } from 'src/app/model/model';
import { FormGroup, FormControl } from '@angular/forms';
import Swal from 'sweetalert2';
import { MatTable, MatTableDataSource } from '@angular/material/table';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';

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

  // ---------- Schedule section
  startHour: number = 23;
  startHourDisplayValue = "23";
  endHour: number = 7;
  endHourDisplayValue = "7";
  days: Record<string, number> = {
    'Sun': 0,
    'Mon': 1,
    'Tue': 2,
    'Wen': 3,
    'Thu': 4,
    'Fri': 5,
    'Sat': 6
  }
  daysClicked: Record<string, boolean> = {
    'Sun': true,
    'Mon': true,
    'Tue': true,
    'Wen': true,
    'Thu': true,
    'Fri': true,
    'Sat': true
  }
  daysList: { key: string, value: number }[] = Object.entries(this.days).map(([key, value]) => ({ key, value }));


  // ---------- Table section
  displayedColumns : string[] = ['command', 'caller', 'callerUsername', 'timestamp'];
  dataSource!: MatTableDataSource<SprinklerCommand>;
  commands : SprinklerCommand[] = [];
  recentCommands: SprinklerCommand[] = [];

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

  filterInitiator: string = '';
  filterEvent: string = '';


  ngOnInit(): void {
    this.sprinklerService.getSprinkler(this.sprinklerId).subscribe((res: any) => {
      console.log("Get sprinkler res:", res)
      this.sprinkler = res;
      if (this.sprinkler!.scheduleMode) {
        this.updateScheduleView()
      } else this.resetScheduleView()
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
          if (this.sprinkler!.scheduleMode) {
            this.updateScheduleView()
          }
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
    if (this.sprinkler!.scheduleMode) {
      this.sprinklerService.putSprinklerTurnOffSchedule(this.sprinklerId).subscribe((res:any) => {
        console.log(res)
      })
    }
      this.resetScheduleView()
  }



  updateScheduleView() {
    this.startHour = this.sprinkler!.scheduleDTO!.startHour
    this.endHour = this.sprinkler!.scheduleDTO!.endHour
    this.startHourDisplayValue = String(this.startHour) + ":00"
    this.endHourDisplayValue = String(this.endHour) + ":00"

    for (let key in this.daysClicked) {
      this.daysClicked[key] = false;
    }

    let weekdayNames = Object.entries(this.days).map(([key, value]) => key);
    for (let dayInt of this.sprinkler!.scheduleDTO!.weekdays) {
      this.daysClicked[weekdayNames[dayInt]] = true
    }
  }

  resetScheduleView() {
    this.startHour = 23
    this.endHour = 7
    this.startHourDisplayValue = String(this.startHour) + ":00"
    this.endHourDisplayValue = String(this.endHour) + ":00"

    for (let key in this.daysClicked) {
      this.daysClicked[key] = true;
    }
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


  displayCaller(caller: string): string {
    switch (caller) {
      case "GATE_EVENT":
        return "Gate"
      case "USER":
        return "User"
      default: return caller
    }
  }

  displayCommand(command: string): string {
    return command.toLowerCase()
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


  applyFilter(): void {
    this.filterApplied = true;

    let filteredEvents: SprinklerCommand[] = [];

    // Filter for date
    if ((this.range.value.start != null && this.range.value.start != null) 
        && this.range.controls.start.valid && this.range.controls.end.valid) { 
        this.sprinklerService.getRangeSprinklerCommands(this.sprinkler!.id, Math.floor(this.range.value.start!.getTime()).toString(), Math.floor(this.range.value.end!.getTime()).toString()).subscribe((res: ApiResponse) => {
          if (res.status == 200) {
            console.log(res)
            filteredEvents = res.data.filter((event: { caller: string; command: string; callerUsername: string;}) =>
              (event.caller.toLowerCase().includes(this.filterInitiator.toLowerCase()) ||
              event.callerUsername.toLowerCase().includes(this.filterInitiator.toLowerCase())) &&
              event.command.toLowerCase().includes(this.filterEvent.toLowerCase())
              );
            this.commands = filteredEvents
            this.dataSource = new MatTableDataSource<SprinklerCommand>(this.commands);
            this.dataSource.sort = this.sort;
            this.dataSource.paginator = this.paginator;
            this.length = this.commands.length;
          }
        });
    } else {
      // Filter for caller and command
      filteredEvents = this.recentCommands.filter(event =>
        (event.caller.toLowerCase().includes(this.filterInitiator.toLowerCase()) ||
        event.callerUsername.toLowerCase().includes(this.filterInitiator.toLowerCase()) &&
        event.command.toLowerCase().includes(this.filterEvent.toLowerCase()
      )));
      this.commands = filteredEvents
      this.dataSource = new MatTableDataSource<SprinklerCommand>(this.commands);
      this.dataSource.sort = this.sort;
      this.dataSource.paginator = this.paginator;
      this.length = this.commands.length;
    }
    
  }

  clearFilter(): void {
    this.filterApplied = false;
    this.filterInitiator = '';
    this.filterEvent = '';
    this.range.reset();
    this.commands = this.recentCommands
    this.dataSource = new MatTableDataSource<SprinklerCommand>(this.commands);
    this.dataSource.sort = this.sort;
    this.dataSource.paginator = this.paginator;
    this.length = this.commands.length;
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
    const part = this.commands.slice(start, end);
    this.dataSource = new MatTableDataSource<SprinklerCommand>(part);

    this.dataSource.data = part;
    this.length = this.commands.length;
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

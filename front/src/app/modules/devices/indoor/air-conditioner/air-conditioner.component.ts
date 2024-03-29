import { HttpClient } from '@angular/common/http';
import { AfterViewInit, Component, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { Observable } from 'rxjs';
import { WebsocketService } from 'src/app/infrastructure/socket/websocket.service';
import { ACIntervalDTO, AirConditionActionDTO, AirConditionerActionRequest, AirConditionerDTO, DeviceDTO } from 'src/app/model/model';
import { environment } from 'src/environments/environment';
import { AuthService } from '../../../auth/service/auth.service';
import { MatTableDataSource } from '@angular/material/table';
import { MatPaginator } from '@angular/material/paginator';
import { OutdoorDeviceService } from '../../outdoor/service/outdoor-device-service';
import { IndoorDeviceService } from '../service/indoor-device.service';
import {COMMA, ENTER} from '@angular/cdk/keycodes';
import {MatChipInputEvent} from '@angular/material/chips';

@Component({
  selector: 'app-air-conditioner',
  templateUrl: './air-conditioner.component.html',
  styleUrls: ['./air-conditioner.component.css']
})
export class AirConditionerComponent implements OnInit, AfterViewInit, OnDestroy {

  currentAction: string = ""
  actionStatus: string = ""
  currentTemp: string = ""
  deviceId: string = ""
  airConditioner: AirConditionerDTO | undefined
  selectedOption: string = ""

  tempForm = new FormGroup({
    temp: new FormControl()
  })

  emailForm = new FormGroup({
    email: new FormControl()
  })

  dateForm = new FormGroup({
    startDate: new FormControl(),
    endDate: new FormControl()
  })

  ELEMENT_DATA: AirConditionActionDTO[] = []
  displayedColumns: string[] = ['email', 'action', 'date'];
  dataSource: any;

  @ViewChild(MatPaginator) paginator: MatPaginator | undefined;

  addOnBlur = true;
  readonly separatorKeysCodes = [ENTER, COMMA] as const;
  intervals: ACIntervalDTO[] = []
  loadingNotDone: boolean = false;

  constructor(private socketService: WebsocketService, private readonly http: HttpClient, private authService: AuthService, private indoorService: IndoorDeviceService) {
    this.indoorService.indoorDeviceId.subscribe((res: string) => {
      this.deviceId = res;
      console.log("air conditioner id " + this.deviceId)
    })
  }

  ngOnInit(): void {
    this.fetchReport(Number(this.deviceId)).subscribe((res: AirConditionActionDTO[]) => {
      console.log(res)
      this.ELEMENT_DATA = res
      this.dataSource = new MatTableDataSource<AirConditionActionDTO>(this.ELEMENT_DATA)
      this.dataSource.paginator = this.paginator;
    })

    this.indoorService.getAirConditioner(this.deviceId).subscribe((airConditioner: AirConditionerDTO) => {
      this.airConditioner = airConditioner
      console.log(this.airConditioner?.regimes)
    })

    this.indoorService.getIntervalsForAC(this.deviceId).subscribe((intervals: ACIntervalDTO[]) => {
      this.intervals = intervals
      console.log(this.intervals)
    })
  }

  ngAfterViewInit(): void {
    const stompClient: any = this.socketService.initWebSocket()

    stompClient.connect({}, () => {
      stompClient.subscribe('/air-conditioner/' + this.deviceId + '/response', (message: { body: string }) => {
        let res: string = message.body
        console.log(res)
        if (res == "Unsupported")
          this.actionStatus = "Unsupported"
        else {
          this.actionStatus = "Success"
          this.currentAction = res
        }

      })

      stompClient.subscribe('/air-conditioner/' + this.deviceId + '/temp', (message: { body: string }) => {
        let res: string = message.body
        console.log(res)
        this.currentTemp = res
      })
    })
  }

  sendAction(request: AirConditionerActionRequest): Observable<string> {
    return this.http.post<string>(environment.apiHost + 'airConditioner/' + this.deviceId + '/action', request)
  }
  
  fetchReport(deviceId: number): Observable<AirConditionActionDTO[]> {
    return this.http.get<AirConditionActionDTO[]>(environment.apiHost + 'airConditioner/' + deviceId + "/actions")
  }

  ngOnDestroy(): void {
    this.socketService.closeWebSocket();
  }

  async cooling(): Promise<void> {
    let request: AirConditionerActionRequest = {
      action: "START COLLING",
      userEmail: this.authService.getEmail()
    }
    await this.sendAction(request).toPromise()
    this.actionStatus = "Trying to start cooling"
  }

  async heating(): Promise<void> {
    let request: AirConditionerActionRequest = {
      action: "START HEATING",
      userEmail: this.authService.getEmail()
    }
    await this.sendAction(request).toPromise()
    this.actionStatus = "Trying to start heating"
  }

  async ventilation(): Promise<void> {
    let request: AirConditionerActionRequest = {
      action: "START VENTILATION",
      userEmail: this.authService.getEmail()
    }
    await this.sendAction(request).toPromise()
    this.actionStatus = "Trying to start ventilation"
  }

  async setTemp(): Promise<void> {
    let request: AirConditionerActionRequest = {
      action: "START TEMP#" + this.tempForm.value.temp,
      userEmail: this.authService.getEmail()
    }
    await this.sendAction(request).toPromise()
    this.actionStatus = "Trying to set temperature"
  }

  async automation(): Promise<void> {
    let request: AirConditionerActionRequest = {
      action: "START AUTOMATIC",
      userEmail: this.authService.getEmail()
    }
    await this.sendAction(request).toPromise()
    this.actionStatus = "Trying to set automatic"
  }

  turnOn(): void {

  }

  async turnOff(): Promise<void> {
    let request: AirConditionerActionRequest = {
      action: "START OFF",
      userEmail: this.authService.getEmail()
    }
    await this.sendAction(request).toPromise()
    this.actionStatus = "Trying to turn off air conditioner"
  }

  async onDropdownChange() {
    console.log("selected " + this.selectedOption)
    if (this.selectedOption == "colling")
      await this.cooling()
    if (this.selectedOption == "heating")
      await this.heating()
    if (this.selectedOption == "ventilation")
      await this.ventilation()
    if (this.selectedOption == "off")
      await this.turnOff()
  }

  fetch(): void {
    this.loadingNotDone = true
    this.fetchReport(Number(this.deviceId)).subscribe((res: AirConditionActionDTO[]) => {
      this.ELEMENT_DATA = res
      this.loadingNotDone = false
      this.dataSource = new MatTableDataSource<AirConditionActionDTO>(this.ELEMENT_DATA)
      this.dataSource.paginator = this.paginator;
    })
  }

  filterEmail(): void {
    this.ELEMENT_DATA = this.ELEMENT_DATA.filter((record: AirConditionActionDTO) =>
      record.email.includes(this.emailForm.value.email || "")
    )
    this.dataSource = new MatTableDataSource<AirConditionActionDTO>(this.ELEMENT_DATA)
    this.dataSource.paginator = this.paginator;
  }

  filterPeriod(): void {
    let startDate: Date = new Date(this.dateForm.value.startDate)
    let endDate: Date = new Date(this.dateForm.value.endDate)

    let ret: AirConditionActionDTO[] = []
    for (let i = 0; i < this.ELEMENT_DATA.length; i++) {
      let date: Date = new Date(this.ELEMENT_DATA[i].date)
      if (date.getTime() >= startDate.getTime() && date.getTime() <= endDate.getTime())
        ret.push(this.ELEMENT_DATA[i])
    }

    this.ELEMENT_DATA = ret
    this.dataSource = new MatTableDataSource<AirConditionActionDTO>(this.ELEMENT_DATA)
    this.dataSource.paginator = this.paginator
  }

  addInterval(event: MatChipInputEvent): void {
    const value = (event.value || '').trim();

    if (value) {
      let tokens: string[] = value.split("-")
      let dto: ACIntervalDTO = {
        id: 0,
        startTime: tokens[0],
        endTime: tokens[1],
        action: tokens[2]
      }
      this.indoorService.addIntervalsForAC(this.deviceId, dto).subscribe((ret: ACIntervalDTO) => {
        this.intervals.push(ret);
      })
    }

    event.chipInput!.clear();
  }

  removeInterval(interval: ACIntervalDTO): void {
    const index = this.intervals.indexOf(interval);

    if (index >= 0) {
      this.indoorService.deleteIntervalsForAC(this.deviceId, interval.id + "").subscribe((ret: object) => {
        this.intervals.splice(index, 1);
      })
    }
  }

}

import { HttpClient } from '@angular/common/http';
import { AfterViewInit, Component, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { Observable } from 'rxjs';
import { WebsocketService } from 'src/app/infrastructure/socket/websocket.service';
import { AirConditionActionDTO, AirConditionerActionRequest, DeviceDTO } from 'src/app/model/model';
import { environment } from 'src/environments/environment';
import { AuthService } from '../../auth/service/auth.service';
import { MatTableDataSource } from '@angular/material/table';
import { MatPaginator } from '@angular/material/paginator';
import { OutdoorDeviceService } from '../outdoor/service/outdoor-device-service';

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
  airConditioner: DeviceDTO | undefined
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

  constructor(private socketService: WebsocketService, private readonly http: HttpClient, private authService: AuthService, private outdoorService: OutdoorDeviceService) {
    this.outdoorService.indoorDeviceId.subscribe((res: string) => {
      this.deviceId = res;
      console.log("air conditioner id " + this.deviceId)

      this.outdoorService.getAirConditioner(this.deviceId).subscribe((airConditioner: DeviceDTO) => {
        this.airConditioner = airConditioner
      })

    })
  }

  ngOnInit(): void {
    this.fetchReport(Number(this.deviceId)).subscribe((res: AirConditionActionDTO[]) => {
      console.log(res)
      this.ELEMENT_DATA = res
      this.dataSource = new MatTableDataSource<AirConditionActionDTO>(this.ELEMENT_DATA)
      this.dataSource.paginator = this.paginator;
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
    if (this.selectedOption == "cooling")
      await this.cooling()
    if (this.selectedOption == "heating")
      await this.heating()
    if (this.selectedOption == "ventilation")
      await this.ventilation()
    if (this.selectedOption == "off")
      await this.turnOff()
  }

  fetch(): void {
    this.fetchReport(Number(this.deviceId)).subscribe((res: AirConditionActionDTO[]) => {
      this.ELEMENT_DATA = res
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

}

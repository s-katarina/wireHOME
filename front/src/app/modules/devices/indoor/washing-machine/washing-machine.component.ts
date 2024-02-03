import { COMMA, ENTER } from '@angular/cdk/keycodes';
import { AfterViewInit, Component, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { MatPaginator } from '@angular/material/paginator';
import { WebsocketService } from 'src/app/infrastructure/socket/websocket.service';
import { AirConditionActionDTO, AirConditionerActionRequest, WMTaskDTO, WashingMachineDTO } from 'src/app/model/model';
import { IndoorDeviceService } from '../service/indoor-device.service';
import { HttpClient } from '@angular/common/http';
import { AuthService } from 'src/app/modules/auth/service/auth.service';
import { MatTableDataSource } from '@angular/material/table';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { flush } from '@angular/core/testing';

@Component({
  selector: 'app-washing-machine',
  templateUrl: './washing-machine.component.html',
  styleUrls: ['./washing-machine.component.css']
})
export class WashingMachineComponent implements OnInit, AfterViewInit, OnDestroy {

  currentAction: string = ""
  actionStatus: string = ""
  deviceId: string = ""
  washingMachine: WashingMachineDTO | undefined
  selectedOption: string = ""

  emailForm = new FormGroup({
    email: new FormControl()
  })

  dateForm = new FormGroup({
    startDate: new FormControl(),
    endDate: new FormControl()
  })

  wmTaskForm = new FormGroup({
    startTime: new FormControl(),
    action: new FormControl()
  })

  ELEMENT_DATA: AirConditionActionDTO[] = []
  displayedColumns: string[] = ['email', 'action', 'date'];
  dataSource: any;

  @ViewChild(MatPaginator) paginator: MatPaginator | undefined;

  addOnBlur = true;
  readonly separatorKeysCodes = [ENTER, COMMA] as const;
  wmtasks: WMTaskDTO[] = []
  loadingNotDone: boolean = false;

  constructor(private socketService: WebsocketService, private readonly http: HttpClient, private authService: AuthService, private indoorService: IndoorDeviceService) { 

    this.indoorService.indoorDeviceId.subscribe((res: string) => {
      this.deviceId = res;
      console.log("washing machine id " + this.deviceId)

      this.indoorService.getWashingMachine(this.deviceId).subscribe((washingMachine: WashingMachineDTO) => {
        this.washingMachine = washingMachine
        console.log(this.washingMachine?.regimes)
      })

      this.indoorService.getTasksForWM(this.deviceId).subscribe((wmtasks: WMTaskDTO[]) => {
        this.wmtasks = wmtasks
        console.log(this.wmtasks)
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
      stompClient.subscribe('/washing-machine/' + this.deviceId + '/response', (message: { body: string }) => {
        let res: string = message.body
        console.log(res)
        if (res == "Unsupported")
          this.actionStatus = "Unsupported"
        else {
          this.actionStatus = "Success"
          this.currentAction = res
        }

      })
    })
  }

  sendAction(request: AirConditionerActionRequest): Observable<string> {
    return this.http.post<string>(environment.apiHost + 'washingMachine/' + this.deviceId + '/action', request)
  }

  fetchReport(deviceId: number): Observable<AirConditionActionDTO[]> {
    return this.http.get<AirConditionActionDTO[]>(environment.apiHost + 'washingMachine/' + deviceId + "/actions")
  }

  ngOnDestroy(): void {
    this.socketService.closeWebSocket();
  }

  async startAction(action: string): Promise<void> {
    let request: AirConditionerActionRequest = {
      action: "START " + action,
      userEmail: this.authService.getEmail()
    }
    await this.sendAction(request).toPromise()
    this.actionStatus = "Trying to start " + action
  }
  // wool30, wool40, wool60, cotton30, cotton40, cotton60, delicate
  async onDropdownChange() {
    console.log("selected " + this.selectedOption)
    if (this.selectedOption == "wool30")
      await this.startAction("WOOL30")
    if (this.selectedOption == "wool40")
      await this.startAction("WOOL40")
    if (this.selectedOption == "wool60")
      await this.startAction("WOOL60")
    if (this.selectedOption == "cotton30")
      await this.startAction("COTTON30")
    if (this.selectedOption == "cotton40")
      await this.startAction("COTTON40")
    if (this.selectedOption == "cotton60")
      await this.startAction("COTTON60")
    if (this.selectedOption == "delicate")
      await this.startAction("DELICATE")
    if (this.selectedOption == "off")
      await this.startAction("OFF")
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

  addWMTask(): void {
    let action: string = this.wmTaskForm.value.action
    let startTime: Date = new Date(this.wmTaskForm.value.startTime)

    if (action != "") {
      let dto: WMTaskDTO = {
        id: 0,
        startTime: startTime.toISOString(),
        action: action
      }
      this.indoorService.addWMTaskForWM(this.deviceId, dto).subscribe((ret: WMTaskDTO) => {
        this.wmtasks.push(ret);
      })
    }

  }

  removeWMTask(wmtask: WMTaskDTO): void {
    const index = this.wmtasks.indexOf(wmtask);

    if (index >= 0) {
      this.indoorService.deleteWMTaskForWM(this.deviceId, wmtask.id + "").subscribe((ret: object) => {
        this.wmtasks.splice(index, 1);
      })
    }
  }

}

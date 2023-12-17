import { HttpClient } from '@angular/common/http';
import { AfterViewInit, Component, OnDestroy, OnInit } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { Observable } from 'rxjs';
import { WebsocketService } from 'src/app/infrastructure/socket/websocket.service';
import { AirConditionerActionRequest } from 'src/app/model/model';
import { environment } from 'src/environments/environment';
import { AuthService } from '../../auth/service/auth.service';

@Component({
  selector: 'app-air-conditioner',
  templateUrl: './air-conditioner.component.html',
  styleUrls: ['./air-conditioner.component.css']
})
export class AirConditionerComponent implements OnInit, AfterViewInit, OnDestroy {

  currentAction: string = ""
  actionStatus: string = ""

  tempForm = new FormGroup({
    temp: new FormControl()
  })

  constructor(private socketService: WebsocketService, private readonly http: HttpClient, private authService: AuthService) { }

  ngOnInit(): void {
  }

  ngAfterViewInit(): void {
    const stompClient: any = this.socketService.initWebSocket()

    stompClient.connect({}, () => {
      stompClient.subscribe('/air-conditioner/3/response', (message: { body: string }) => {
        let res: string = message.body
        console.log("res: " + res)
      })
    })
  }

  sendAction(request: AirConditionerActionRequest): Observable<string> {
    return this.http.post<string>(environment.apiHost + 'airConditioner/3/action', request)
  }

  ngOnDestroy(): void {
    
  }

  async cooling(): Promise<void> {
    let request: AirConditionerActionRequest = {
      action: "START COOLING",
      userEmail: this.authService.getEmail()
    }
    await this.sendAction(request).toPromise()
    this.actionStatus = "Trying to start cooling"
  }

  heating(): void {

  }

  ventilation(): void {

  }

  setTemp(): void {

  }

  turnOn(): void {

  }

  turnOff(): void {

  }

  

}

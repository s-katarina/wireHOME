import { AfterViewInit, Component, OnInit } from '@angular/core';
import { WebsocketService } from 'src/app/infrastructure/socket/websocket.service';

@Component({
  selector: 'app-lamp',
  templateUrl: './lamp.component.html',
  styleUrls: ['./lamp.component.css']
})
export class LampComponent implements OnInit, AfterViewInit {

  constructor(private socketService: WebsocketService,) { }

  ngOnInit(): void {
  }

  ngAfterViewInit(): void {
    const stompClient: any = this.socketService.initWebSocket()

    stompClient.connect({}, () => {
      stompClient.subscribe('/lamp/8', (message: { body: string }) => {
        console.log("stiglo iz web socketa")
        console.log(message)
      })
    })
  }

}

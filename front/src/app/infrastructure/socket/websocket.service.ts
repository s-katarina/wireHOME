import { Injectable } from '@angular/core';
import { environment } from 'src/environments/environment';
import * as Stomp from 'stompjs'
import * as SockJS from 'sockjs-client'

@Injectable({
  providedIn: 'root'
})
export class WebsocketService {
  stompClient: any

  constructor() { }

  initWebSocket () {
    const ws = new SockJS(environment.socketHost)
    this.stompClient = Stomp.over(ws)
    this.stompClient.debug = null
    // let that = this;
    // this.stompClient.connect({}, function () {
    //   that.openGlobalSocket();
    // });
    return this.stompClient
  }

  closeWebSocket() {
    if (this.stompClient && this.stompClient.connected) {
      this.stompClient.disconnect();
    }
  }
}

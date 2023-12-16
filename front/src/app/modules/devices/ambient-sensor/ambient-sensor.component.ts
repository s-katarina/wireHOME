import { AfterViewInit, Component, OnDestroy, OnInit } from '@angular/core';
import { WebsocketService } from 'src/app/infrastructure/socket/websocket.service';
import * as Chart from 'chart.js';
import { Observable } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { environment } from 'src/environments/environment';
import { AmbientSensorTempHumDTO, AmbientSensorDateValueDTO } from 'src/app/model/model';

@Component({
  selector: 'app-ambient-sensor',
  templateUrl: './ambient-sensor.component.html',
  styleUrls: ['./ambient-sensor.component.css']
})
export class AmbientSensorComponent implements OnInit, AfterViewInit, OnDestroy {

  public chart: any;
  labels: string[] = []
  data: number[] = []

  constructor(private socketService: WebsocketService, private readonly http: HttpClient) { }

  ngOnInit(): void {

    let now: number = Math.floor(Date.now() / 1000)
    let before: number = now - 3 * 60 * 60

    this.getTemp(before, now).subscribe((res: AmbientSensorTempHumDTO) => {
      let tempLabels: string[] = res.temp.dates
      let tempValues: number[] = res.temp.values

      this.labels = tempLabels
      this.data = tempValues

      this.makeChart()
    })

  }

  makeChart(): void {
    if (this.chart)
      this.chart.destroy()

    this.chart = new Chart("MyChart", {
      type: 'line', //this denotes tha type of chart

      data: {// values on X-Axis
        labels: this.labels, 
	       datasets: [
          {
            label: "Temperature",
            data: this.data,
            backgroundColor: 'blue'
          } 
        ]
      },
      options: {
        aspectRatio: 2.5
      }
      
    });

  }

  ngAfterViewInit(): void {
    const stompClient: any = this.socketService.initWebSocket()

    stompClient.connect({}, () => {
      stompClient.subscribe('/ambient-sensor/2/temp', (message: { body: number }) => {
        let temp: number = message.body
        console.log("temp: " + temp)

        this.labels.shift()
        this.data.shift()

        this.labels.push((new Date()).toISOString())
        this.data.push(temp)

        this.chart.update()
      })

      stompClient.subscribe('/ambient-sensor/2/hum', (message: { body: number }) => {
        let hum: number = message.body
        console.log("hum: " + hum)
      })
    })
  }

  getTemp (from: number, to: number): Observable<AmbientSensorTempHumDTO> {
    return this.http.get<AmbientSensorTempHumDTO>(environment.apiHost + 'ambientSensor/2/values?from=' + from + "&to=" + to)
  }

  ngOnDestroy(): void {
    
  }

}

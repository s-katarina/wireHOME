import { AfterViewInit, Component, OnDestroy, OnInit } from '@angular/core';
import { WebsocketService } from 'src/app/infrastructure/socket/websocket.service';
import * as Chart from 'chart.js';
import { Observable } from 'rxjs';
import { HttpClient } from '@angular/common/http';
import { environment } from 'src/environments/environment';
import { AmbientSensorTempHumDTO, AmbientSensorDateValueDTO, GraphPoint } from 'src/app/model/model';
import { FormControl, FormGroup, Validators } from '@angular/forms';

@Component({
  selector: 'app-ambient-sensor',
  templateUrl: './ambient-sensor.component.html',
  styleUrls: ['./ambient-sensor.component.css']
})
export class AmbientSensorComponent implements OnInit, AfterViewInit, OnDestroy {

  public realtimeChart: any;
  public reportChart: any;

  tempData: GraphPoint[] = []
  humData: GraphPoint[] = []

  currentTemp: number = NaN
  currentHum: number = NaN
  deviceId: string = "5"


  reportTempData: GraphPoint[] = []
  reportHumData: GraphPoint[] = []

  dateForm = new FormGroup({
    startDate: new FormControl(),
    endDate: new FormControl()
  })

  constructor(private socketService: WebsocketService, private readonly http: HttpClient) { }

  ngOnInit(): void {

    let now: number = Math.floor(Date.now() / 1000)
    let before: number = now - 1 * 60 * 60

    this.getTempAndHum(before, now).subscribe((res: AmbientSensorTempHumDTO) => {
      
      for (let i = 0; i < res.length; i++) {
        let tempPoint: GraphPoint = {
          x: res.temp.dates[i],
          y: res.temp.values[i]
        }
        this.tempData.push(tempPoint)
        this.reportTempData.push(tempPoint)

        let humPoint: GraphPoint = {
          x: res.hum.dates[i],
          y: res.hum.values[i]
        }
        this.humData.push(humPoint)
        this.reportHumData.push(humPoint)
      }

      this.tempData.sort((a: GraphPoint, b: GraphPoint) => Date.parse(a.x) - Date.parse(b.x))
      this.humData.sort((a: GraphPoint, b: GraphPoint) => Date.parse(a.x) - Date.parse(b.x))

      this.reportTempData.sort((a: GraphPoint, b: GraphPoint) => Date.parse(a.x) - Date.parse(b.x))
      this.reportHumData.sort((a: GraphPoint, b: GraphPoint) => Date.parse(a.x) - Date.parse(b.x))
      
      if (this.tempData.length > 0)
        this.currentTemp = this.tempData[this.tempData.length - 1].y
      if (this.humData.length > 0)
        this.currentHum = this.humData[this.humData.length - 1].y

      this.makeRealtimeChart()
      this.makeReportChart()
    })

  }

  makeRealtimeChart(): void {
    if (this.realtimeChart)
      this.realtimeChart.destroy()

    this.realtimeChart = new Chart("realtimeChart", {
      type: 'line',

      data: {
	       datasets: [
          {
            label: "Temperature",
            data: this.tempData,
            borderColor: "blue",
            fill: false
          },
          {
            label: "Humidity",
            data: this.humData,
            borderColor: "green",
            fill: false
          } 
        ]
      },
      options: {
        scales: {
          xAxes: [{
            type: 'time',
            time: {
              unit: 'second'
            },
            distribution: 'series'
          }]
        }
      }
      
    });

  }

  ngAfterViewInit(): void {
    const stompClient: any = this.socketService.initWebSocket()

    stompClient.connect({}, () => {
      stompClient.subscribe('/ambient-sensor/' + this.deviceId + '/temp', (message: { body: number }) => {
        let temp: number = message.body
        console.log("temp: " + temp)

        let now: number = Math.floor(Date.now() / 1000)
        let firstInChart: number = Math.floor((new Date(this.tempData[0].x)).getTime() / 1000)

        if (firstInChart + 60 * 60 < now)
          this.tempData.shift()

        let point: GraphPoint = {
          x: (new Date()).toISOString(),
          y: temp
        }

        this.tempData.push(point)
        this.currentTemp = temp

        this.realtimeChart.update()
      })

      stompClient.subscribe('/ambient-sensor/' + this.deviceId + '/hum', (message: { body: number }) => {
        let hum: number = message.body
        console.log("hum: " + hum)

        let now: number = Math.floor(Date.now() / 1000)
        let firstInChart: number = Math.floor((new Date(this.humData[0].x)).getTime() / 1000)

        if (firstInChart + 60 * 60 < now)
          this.humData.shift()

        let point: GraphPoint = {
          x: (new Date()).toISOString(),
          y: hum
        }

        this.humData.push(point)
        this.currentHum = hum

        this.realtimeChart.update()
      })
    })
  }

  getTempAndHum(from: number, to: number): Observable<AmbientSensorTempHumDTO> {
    return this.http.get<AmbientSensorTempHumDTO>(environment.apiHost + 'ambientSensor/' + this.deviceId + '/values?from=' + from + "&to=" + to)
  }

  ngOnDestroy(): void {
    this.socketService.closeWebSocket();
  }

  report(hours: number): void {

    this.reportTempData = []
    this.reportHumData = []

    let now: number = Math.floor(Date.now() / 1000)
    let before: number = now - hours * 60 * 60

    this.getTempAndHum(before, now).subscribe((res: AmbientSensorTempHumDTO) => {
      // console.log(res)
      for (let i = 0; i < res.length; i++) {
        let tempPoint: GraphPoint = {
          x: res.temp.dates[i],
          y: res.temp.values[i]
        }
        this.reportTempData.push(tempPoint)

        let humPoint: GraphPoint = {
          x: res.hum.dates[i],
          y: res.hum.values[i]
        }
        this.reportHumData.push(humPoint)
      }

      this.reportTempData.sort((a: GraphPoint, b: GraphPoint) => Date.parse(a.x) - Date.parse(b.x))
      this.reportHumData.sort((a: GraphPoint, b: GraphPoint) => Date.parse(a.x) - Date.parse(b.x))
      
      this.makeReportChart() // ne znam zasto update ne radi nego mora da se ponovo napravi graf
    })
  }

  makeReportChart(): void {
    if (this.reportChart)
      this.reportChart.destroy()

    this.reportChart = new Chart("reportChart", {
      type: 'line',

      data: {
	       datasets: [
          {
            label: "Temperature",
            data: this.reportTempData,
            borderColor: "blue",
            fill: false
          },
          {
            label: "Humidity",
            data: this.reportHumData,
            borderColor: "green",
            fill: false
          } 
        ]
      },
      options: {
        scales: {
          xAxes: [{
            type: 'time',
            time: {
              unit: 'second'
            },
            distribution: 'series'
          }]
        }
      }
      
    });
  }

  reportPeriod(): void {

    let startDate: Date = new Date(this.dateForm.value.startDate)
    let endDate: Date = new Date(this.dateForm.value.endDate)

    const mesecDana = 30 * 24 * 60 * 60 * 1000; // Broj milisekundi u mesecu
    const razlika = Math.abs(endDate.getTime() - startDate.getTime());

    if (razlika > mesecDana) {
      alert("Difference is greater than 1 month")
      return
    } 

    this.reportTempData = []
    this.reportHumData = []

    let from: number = Math.floor(startDate.getTime() / 1000) // u sekundama
    let to: number = Math.floor(endDate.getTime() / 1000)

    this.getTempAndHum(from, to).subscribe((res: AmbientSensorTempHumDTO) => {
      // console.log(res)
      for (let i = 0; i < res.length; i++) {
        let tempPoint: GraphPoint = {
          x: res.temp.dates[i],
          y: res.temp.values[i]
        }
        this.reportTempData.push(tempPoint)

        let humPoint: GraphPoint = {
          x: res.hum.dates[i],
          y: res.hum.values[i]
        }
        this.reportHumData.push(humPoint)
      }

      this.reportTempData.sort((a: GraphPoint, b: GraphPoint) => Date.parse(a.x) - Date.parse(b.x))
      this.reportHumData.sort((a: GraphPoint, b: GraphPoint) => Date.parse(a.x) - Date.parse(b.x))
      
      this.makeReportChart() // ne znam zasto update ne radi nego mora da se ponovo napravi graf
    })
  }

}

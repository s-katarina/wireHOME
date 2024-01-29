import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { ApiResponse, Battery, ChartData, GraphDTO, PyChartDTO, SolarPanel } from 'src/app/model/model';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root'
})
export class LargeEnergyService {
  
  getGateEvents(id: string): Observable<ApiResponse> {
    return this.http.get<ApiResponse>(environment.apiHost + `device/largeEnergy/${id}/recent`)
  }

  getRangeGateEvents(id: string, start: string, end: string): Observable<ApiResponse> {
    const params = new HttpParams().set('start', start)
                                    .set('end', end);

    return this.http.get<ApiResponse>(environment.apiHost + `device/largeEnergy/${id}/range`, {params})
  }
  
  getBattery(id: string): Observable<Battery> {
    return this.http.get<Battery>(environment.apiHost + `device/largeEnergy/battery/${id}`)
  }

  postOn(id:string): Observable<any> {
    const options: any = {
      responseType: 'string'
    }
    return this.http.post<any>(environment.apiHost + `device/on/${id}`, options)
  }
  postOff(id:string): Observable<any> {
    const options: any = {
      responseType: 'string'
    }
    return this.http.post<any>(environment.apiHost + `device/off/${id}`, options)
  }
  getSolarPanel(id: string) : Observable<SolarPanel[]> {
    return this.http.get<SolarPanel[]>(environment.apiHost + `device/largeEnergy/solar/${id}`)
  }
  getPropertyReadingFrom(propertyId: string, dateFrom: string, dateTo: string, measurment: string) {
    const options: any = {
      responseType: 'json'
    }
    
    return this.http.post<GraphDTO[]>(environment.apiHost + 'device/largeEnergy/propertyEnergy', 
    {
      id: propertyId,
      from: dateFrom,
      to: dateTo,
      measurement: measurment,
    }, options)  
  }
  
  getSolarPlatformReadingFrom(panelId: string, dateFrom: string | null, dateTo: string | null, measurment: string): Observable<any> {
    const options: any = {
      responseType: 'json'
    }
    
    return this.http.post<GraphDTO[]>(environment.apiHost + 'device/largeEnergy/panelReadings', 
    {
      id: panelId,
      from: dateFrom,
      to: dateTo,
      measurement: measurment,
    }, options)  
  }


  getDeviceOnlineOfflinePyChart(id: string, start: string, end: string) : Observable<PyChartDTO[]> {
    const params = new HttpParams().set('start', start)
                                    .set('end', end);
    return this.http.get<PyChartDTO[]>(environment.apiHost + `device/onlinePercent/${id}`, {params})
  }
  
  constructor(private readonly http: HttpClient) { }

  private selectedDevice = new BehaviorSubject<string>("");
  selectedDeviceId$ = this.selectedDevice.asObservable();

  setSelectedDeviceId(id: string) {
    this.selectedDevice.next(id);
  }


}

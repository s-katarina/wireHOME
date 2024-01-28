import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { ApiResponse, Battery, Charger, ChartData, GraphDTO, PyChartDTO, SolarPanel } from 'src/app/model/model';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root'
})
export class LargeEnergyService {
  changePort(id:string, licencePlate: number): Observable<any> {
    const params = new HttpParams().set('val', licencePlate);
    return this.http.put<any>(environment.apiHost + `device/largeEnergy/charger/${id}/port`, {}, {params})
  }

  getCharger(id: string): Observable<Charger> {
    return this.http.get<Charger>(environment.apiHost + `device/largeEnergy/charger/${id}`)
  }
  
  getGateEvents(id: string, measurement: string): Observable<ApiResponse> {
    const params = new HttpParams().set('measurement', measurement)
    return this.http.get<ApiResponse>(environment.apiHost + `device/largeEnergy/${id}/recent`, {params})
  }

  getRangeGateEvents(id: string, start: string, end: string, measurement: string): Observable<ApiResponse> {
    const params = new HttpParams().set('start', start)
                                    .set('end', end)
                                    .set('measurement', measurement)

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


  getDeviceOnlineOfflinePyChart(id: string) : Observable<PyChartDTO[]> {
    return this.http.get<PyChartDTO[]>(environment.apiHost + `device/onlinePercent/${id}`)
  }
  
  constructor(private readonly http: HttpClient) { }

  private selectedDevice = new BehaviorSubject<string>("");
  selectedDeviceId$ = this.selectedDevice.asObservable();

  setSelectedDeviceId(id: string) {
    this.selectedDevice.next(id);
  }


}

import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { Battery, ChartData, GraphDTO, SolarPanel } from 'src/app/model/model';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root'
})
export class LargeEnergyService {
  getBattery(batteryId: string): Observable<Battery[]> {
    throw new Error('Method not implemented.');
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
    return this.http.get<SolarPanel[]>(environment.apiHost + `device/solar/${id}`)
  }
  getPropertyReadingFrom(propertyId: string, dateFrom: string, dateTo: string, measurment: string) {
    const options: any = {
      responseType: 'json'
    }
    
    return this.http.post<GraphDTO[]>(environment.apiHost + 'device/solar/propertyEnergy', 
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
    
    return this.http.post<GraphDTO[]>(environment.apiHost + 'device/solar/panelReadings', 
    {
      id: panelId,
      from: dateFrom,
      to: dateTo,
      measurement: measurment,
    }, options)  
  }
  
  constructor(private readonly http: HttpClient) { }

  private selectedDevice = new BehaviorSubject<string>("");
  selectedDeviceId$ = this.selectedDevice.asObservable();

  setSelectedDeviceId(id: string) {
    this.selectedDevice.next(id);
  }


}

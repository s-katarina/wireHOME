import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { ChartData, GraphDTO } from 'src/app/model/model';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root'
})
export class LargeEnergyService {
  getSolarPlatformReadingFrom(panelId: string, dateFrom: string | null, dateTo: string | null): Observable<any> {
    const options: any = {
      responseType: 'json'
    }
    return this.http.post<GraphDTO[]>(environment.apiHost + 'device/solar/panelReadings', 
    {
      id: panelId,
      from: dateFrom,
      to: dateTo,
    }, options)  
  }
  
  constructor(private readonly http: HttpClient) { }

  private selectedDevice = new BehaviorSubject<string>("");
  selectedDeviceId$ = this.selectedDevice.asObservable();

  setSelectedDeviceId(id: string) {
    this.selectedDevice.next(id);
  }


}

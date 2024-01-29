import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { AirConditionerDTO, DeviceDTO } from 'src/app/model/model';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root'
})
export class IndoorDeviceService {

  private selectedIndoorDeviceId = new BehaviorSubject<string>("");
  indoorDeviceId = this.selectedIndoorDeviceId.asObservable();

  setSelectedIndoorDeviceId(id: string) {
    this.selectedIndoorDeviceId.next(id);
  }

  constructor(private readonly http: HttpClient) { }

  getAmbientSensor(id: string): Observable<DeviceDTO> {
    return this.http.get<DeviceDTO>(environment.apiHost + `ambientSensor/${id}`)
  }

  getAirConditioner(id: string): Observable<AirConditionerDTO> {
    return this.http.get<AirConditionerDTO>(environment.apiHost + `airConditioner/${id}`)
  }
}

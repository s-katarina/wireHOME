import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { Lamp } from 'src/app/model/model';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root'
})
export class OutdoorDeviceService {

  private selectedDeviceId = new BehaviorSubject<string>("");
  selectedLampId = this.selectedDeviceId.asObservable();

  setSelectedDeviceId(id: string) {
    this.selectedDeviceId.next(id);
  }

  constructor(private readonly http: HttpClient) { }

  getLamp(id:string): Observable<Lamp[]> {
    return this.http.get<Lamp[]>(environment.apiHost + `lamp/${id}`)
  }

  
  postOn(id:string): Observable<any> {
    return this.http.post<any>(environment.apiHost + `device/on/${id}`, {})
  }
  postOff(id:string): Observable<any> {
    return this.http.post<any>(environment.apiHost + `device/off/${id}`, {})
  }

  postBulbOn(id:string): Observable<any> {
    return this.http.put<any>(environment.apiHost + `lamp/${id}/bulb-on`, {})
  }
  postBulbOff(id:string): Observable<any> {
    return this.http.put<any>(environment.apiHost + `lamp/${id}/bulb-off`, {})
  }

  postAutomaticOnOff(id:string, automatic: boolean): Observable<any> {
    const params = new HttpParams().set('val', automatic.toString());

    return this.http.put<any>(environment.apiHost + `lamp/${id}/automatic`, {}, {params})
  }

}

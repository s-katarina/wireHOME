import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { ApiResponse, DeviceDTO, Gate, Lamp, Sprinkler } from 'src/app/model/model';
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

  getLamp(id:string): Observable<Lamp> {
    return this.http.get<Lamp>(environment.apiHost + `lamp/${id}`)
  }

  getGate(id:string): Observable<Gate> {
    return this.http.get<Gate>(environment.apiHost + `gate/${id}`)
  }

  getSprinkler(id:string): Observable<Sprinkler> {
    return this.http.get<Sprinkler>(environment.apiHost + `sprinkler/${id}`)
  }
  
  putSprinklerOnOff(id: string, newOn: boolean): Observable<any> {
    const params = new HttpParams().set('val', newOn);
    return this.http.put<any>(environment.apiHost + `sprinkler/${id}/on`, {}, {params})
  }

  putSprinklerSchedule(id: string, startHour: number, endHour: number, weekdays: number[]): Observable<any> {
    return this.http.put<any>(environment.apiHost + `sprinkler/${id}/schedule`, {
      "startHour": startHour,
      "endHour": endHour,
      "weekdays": weekdays
    }, {})
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

  postRegimeOnOff(id:string, isPublic: boolean): Observable<any> {
    const params = new HttpParams().set('public', isPublic.toString());

    return this.http.put<any>(environment.apiHost + `gate/${id}/regime`, {}, {params})
  }

  postOpenOnOff(id:string, open: boolean): Observable<any> {
    const params = new HttpParams().set('val', open.toString());

    return this.http.put<any>(environment.apiHost + `gate/${id}/open`, {}, {params})
  }

  getGateEvents(id: string): Observable<ApiResponse> {
    return this.http.get<ApiResponse>(environment.apiHost + `gate/${id}/recent`)
  }

  getRangeGateEvents(id: string, start: string, end: string): Observable<ApiResponse> {
    const params = new HttpParams().set('start', start)
                                    .set('end', end);

    return this.http.get<ApiResponse>(environment.apiHost + `gate/${id}/range`, {params})
  }

  putLicencePlate(id:string, licencePlate: string): Observable<any> {
    const params = new HttpParams().set('val', licencePlate);

    return this.http.put<any>(environment.apiHost + `gate/${id}/licencePlate`, {}, {params})
  }

  getRangeLightSensor(id: string, start: string, end: string): Observable<ApiResponse> {
    const params = new HttpParams().set('start', start)
                                    .set('end', end);

    return this.http.get<ApiResponse>(environment.apiHost + `lamp/${id}/range`, {params})
  }

  getRangeBulb(id: string, start: string, end: string): Observable<ApiResponse> {
    const params = new HttpParams().set('start', start)
                                    .set('end', end);

    return this.http.get<ApiResponse>(environment.apiHost + `lamp/${id}/range/bulb`, {params})
  }


}

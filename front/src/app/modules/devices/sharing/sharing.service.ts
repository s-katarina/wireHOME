import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { SharedDeviceDTO, SharedPropertyDTO } from 'src/app/model/model';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root'
})
export class SharingService {

  constructor(private readonly http: HttpClient) { }

  getSharedWithProperties(id: string): Observable<SharedPropertyDTO[]> {
    return this.http.get<SharedPropertyDTO[]>(environment.apiHost + `sharing/property/${id}`)
  }

  getSharedWithDevices(id: string): Observable<SharedDeviceDTO[]> {
    return this.http.get<SharedDeviceDTO[]>(environment.apiHost + `sharing/device/${id}`)
  }

}

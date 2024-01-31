import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { AppUserDTO, DeviceDTO, ShareActionDTO, SharedDeviceDTO, SharedPropertyDTO } from 'src/app/model/model';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root'
})
export class SharingService {

  constructor(private readonly http: HttpClient) { }

  getDevicesForOwnerOfProperty(ownerId: string): Observable<DeviceDTO[]> {
    return this.http.get<DeviceDTO[]>(environment.apiHost + `device/owner/${ownerId}`)
  }

  getSharedWithProperties(id: string): Observable<SharedPropertyDTO[]> {
    return this.http.get<SharedPropertyDTO[]>(environment.apiHost + `sharing/property/${id}`)
  }

  shareProperty(dto: ShareActionDTO): Observable<SharedPropertyDTO> {
    return this.http.post<SharedPropertyDTO>(environment.apiHost + `sharing/property`, dto)
  }

  removeProperty(dto: ShareActionDTO): Observable<object> {
    return this.http.post<object>(environment.apiHost + `sharing/property/delete`, dto)
  }

  getSharedWithDevices(id: string): Observable<SharedDeviceDTO[]> {
    return this.http.get<SharedDeviceDTO[]>(environment.apiHost + `sharing/device/${id}`)
  }

  shareDevice(dto: ShareActionDTO): Observable<SharedDeviceDTO> {
    return this.http.post<SharedDeviceDTO>(environment.apiHost + `sharing/device`, dto)
  }

  removeDevice(dto: ShareActionDTO): Observable<object> {
    return this.http.post<object>(environment.apiHost + `sharing/device/delete`, dto)
  }

}

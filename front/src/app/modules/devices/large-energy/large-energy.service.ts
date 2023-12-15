import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class LargeEnergyService {
  
  constructor(private readonly http: HttpClient) { }

  private selectedDevice = new BehaviorSubject<string>("");
  selectedDeviceId$ = this.selectedDevice.asObservable();

  setSelectedDeviceId(id: string) {
    this.selectedDevice.next(id);
  }


}

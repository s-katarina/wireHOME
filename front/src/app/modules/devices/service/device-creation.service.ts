import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { DeviceRequestDTO } from 'src/app/model/model';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root'
})
export class DeviceCreationService {

  constructor(private readonly http: HttpClient) { }

  createDevice(device: DeviceRequestDTO, deviceType: string, machineRegimes: string[] = [], airConditionerRegimes: string[] = [], capacity=0) {
    switch(deviceType) {
      case 'ambientSensore':
        return this.createAmbientalSensor(device);
      case 'airConditioner':
        device.regimes = airConditionerRegimes;
        return this.createAirConditioner(device);
      case 'washingMashine':
        device.regimes = machineRegimes;
        return this.createWashingMachine(device);
      case 'lamp':
        return this.createLamp(device);
      case 'gate':
        return this.createGate(device);
      case 'sprinklers':
        return this.createSprinkler(device);
      case 'solarPanel':
        return this.createSolarPanel(device);
      case 'houseBatery':
        device.capacity = capacity;
        return this.createBattery(device);
      case 'charger':
        return this.createCharger(device);
      default:
        return this.createAmbientalSensor(device);
    }
  }

  getRegimes() : Observable<string[]> {
    return this.http.get<string[]>(environment.apiHost + 'device/regimes')
  }

  getAirConditionerRegimes() : Observable<string[]> {
    return this.http.get<string[]>(environment.apiHost + 'device/regimesAirConditioner')
  }

  getWashingMachineRegimes() : Observable<string[]> {
    return this.http.get<string[]>(environment.apiHost + 'device/regimesWashingMachine')
  }

  createAmbientalSensor (device: DeviceRequestDTO): Observable<any> {
    const options: any = {
      responseType: 'text'
    }
    return this.http.post<string>(environment.apiHost + 'device/ambientSensor', this.makeResponceDTO(device), options)
  }

  createAirConditioner (device: DeviceRequestDTO): Observable<any> {
    const options: any = {
      responseType: 'text'
    }
    return this.http.post<string>(environment.apiHost + 'device/airConditioner', this.makeResponceDTO(device), options)
  }

  createBattery (device: DeviceRequestDTO): Observable<any> {
    const options: any = {
      responseType: 'text'
    }
    return this.http.post<string>(environment.apiHost + 'device/battery', this.makeResponceDTO(device), options)
  }

  createCharger (device: DeviceRequestDTO): Observable<any> {
    const options: any = {
      responseType: 'text'
    }
    return this.http.post<string>(environment.apiHost + 'device/charger', this.makeResponceDTO(device), options)
  }

  createGate (device: DeviceRequestDTO): Observable<any> {
    const options: any = {
      responseType: 'text'
    }
    return this.http.post<string>(environment.apiHost + 'device/gate', this.makeResponceDTO(device), options)
  }

  createLamp (device: DeviceRequestDTO): Observable<any> {
    const options: any = {
      responseType: 'text'
    }
    return this.http.post<string>(environment.apiHost + 'device/lamp', this.makeResponceDTO(device), options)
  }

  createSolarPanel (device: DeviceRequestDTO): Observable<any> {
    const options: any = {
      responseType: 'text'
    }
    return this.http.post<string>(environment.apiHost + 'device/solarPanel', this.makeResponceDTO(device), options)
  }

  createSprinkler (device: DeviceRequestDTO): Observable<any> {
    const options: any = {
      responseType: 'text'
    }
    return this.http.post<string>(environment.apiHost + 'device/sprinkler', this.makeResponceDTO(device), options)
  }

  createWashingMachine (device: DeviceRequestDTO): Observable<any> {
    const options: any = {
      responseType: 'text'
    }
    return this.http.post<string>(environment.apiHost + 'device/washingMachine', this.makeResponceDTO(device), options)
  }

  private makeResponceDTO(device: DeviceRequestDTO): any {
    return {
      modelName: device.modelName,
      usesElectricity: device.usesElectricity,
      consumptionAmount: device.consumptionAmount,
      propertyId: device.propertyId,
      regimes: device.regimes,
      minTemp: device.minTemp,
      maxTemp: device.maxTemp,
      panelSize: device.panelSize,
      efficiency: device.efficiency,
      capacity: device.capacity,
      portNumber: device.portNumber,
    };
  }
}


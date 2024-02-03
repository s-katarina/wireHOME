import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { BarChartDTO, ByTimeOfDay, CityDTO, CityOverview, DeviceDTO, GraphDTO, LabeledGraphDTO, PropertyDTO, PropertyRequestDTO, PyChartDTO } from 'src/app/model/model';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root'
})
export class PropertyServiceService {
  

  private propertySource = new BehaviorSubject<PropertyDTO | undefined>(undefined);
  currentProperty = this.propertySource.asObservable();

  setProperty(property: PropertyDTO | undefined) {
    this.propertySource.next(property);
  }

  private citySource = new BehaviorSubject<CityOverview | undefined>(undefined);
  currentCity = this.citySource.asObservable();

  setCity(city: CityOverview | undefined) {
    this.citySource.next(city);
  }

  constructor(private readonly http: HttpClient) { }

  private selectedProperty = new BehaviorSubject<string>("");
  selectedPropertyId$ = this.selectedProperty.asObservable();

  setSelectedPropertyId(PropertyId: string) {
    this.selectedProperty.next(PropertyId);
  }

  private selectedCity= new BehaviorSubject<string>("");
  selectedCityId$ = this.selectedCity.asObservable();

  setSelectedCityId(cityId: string) {
    this.selectedCity.next(cityId);
  }

  getCities() : Observable<CityDTO[]> {
    return this.http.get<CityDTO[]>(environment.apiHost + 'property/city')
  }

  create(property: PropertyRequestDTO): Observable<any> {
    const options: any = {
      responseType: 'text'
    }
    return this.http.post<string>(environment.apiHost + 'property', {
      propertyType: property.propertyType,
      address: property.address,
      cityId: property.cityId,
      area: property.area,
      floorCount: property.floorCount
    }, options)
  }

  getProperties(): Observable<PropertyDTO[]> {
    return this.http.get<PropertyDTO[]>(environment.apiHost + 'property')
  }

  getPendingProperties(): Observable<PropertyDTO[]> {
    return this.http.get<PropertyDTO[]>(environment.apiHost + 'property/pending')
  }

  acceptPending(id: string): Observable<any> {
    const options: any = {
      responseType: 'text'
    }
    return this.http.put<PropertyDTO>(environment.apiHost + `property/pending/accept/${id}`, {}, options)
  }

  rejectPending(id: string, rejectionReason: string): Observable<any> {

    const headers = new HttpHeaders({
      'Content-Type': 'application/json'
    });
    const requestBody = {rejectionReason: rejectionReason}
    return this.http.put<PropertyDTO>(environment.apiHost + `property/pending/reject/${id}`, requestBody, {headers})
  }


  getApliences(id:string): Observable<DeviceDTO[]> {
    return this.http.get<DeviceDTO[]>(environment.apiHost + `device/appliances/${id}`)
  }

  getOutdoor(id:string): Observable<DeviceDTO[]> {
    return this.http.get<DeviceDTO[]>(environment.apiHost + `device/outdoor/${id}`)
  }

  getEnergyDevices(id:string): Observable<DeviceDTO[]> {
    return this.http.get<DeviceDTO[]>(environment.apiHost + `device/energyDevices/${id}`)
  }

  getAcceptedProperties(start: number, end: number): Observable<PropertyDTO[]> {
    const params = new HttpParams().set('start', start)
    .set('end', end);
    return this.http.get<PropertyDTO[]>(environment.apiHost + 'property/accepted', {params})
  }

  getCityForOverview(start: number, end: number): Observable<CityOverview[]> {
    const params = new HttpParams().set('start', start)
    .set('end', end);
    return this.http.get<CityOverview[]>(environment.apiHost + 'property/byCity', {params})
  }

  getByCityChart(start: number, end: number): Observable<PyChartDTO[]> {
    const params = new HttpParams().set('start', start)
                                    .set('end', end);
    return this.http.get<PyChartDTO[]>(environment.apiHost + 'property/byCityChart', {params})
  }

  getByMonthProperty(id: string, year: number, measurement:string): Observable<BarChartDTO[]> {
    const params = new HttpParams().set('year', year)
                                  .set("measurement", measurement)
    return this.http.get<BarChartDTO[]>(environment.apiHost + `property/byMonthProperty/${id}`, {params})
  }

  getTimeOfDay(id: string | undefined, start: number, end: number): Observable<ByTimeOfDay[]> {
    const params = new HttpParams().set('start', start)
                                  .set("end", end)
    return this.http.get<ByTimeOfDay[]>(environment.apiHost + `property/byTimeOfDay/${id}`, {params})
  }
  getByDeviceTypeForProperty(start: number, end: number, id: string): Observable<PyChartDTO[]> {
    const params = new HttpParams().set('start', start)
                                  .set("end", end)
    return this.http.get<PyChartDTO[]>(environment.apiHost + `property/byDeviceType/${id}`, {params})
  }

  getCityReadingFrom(cityId: number, dateFrom: number, dateTo: number, measurment: string) {
    const options: any = {
      responseType: 'json'
    }
    
    return this.http.post<GraphDTO[]>(environment.apiHost + 'property/propertyEnergy', 
    {
      id: cityId,
      from: dateFrom,
      to: dateTo,
      measurement: measurment,
    }, options)  
  }

  getPropertyByDayReadingFrom(cityId: string, dateFrom: number, dateTo: number, measurment: string) {
    const options: any = {
      responseType: 'json'
    }
    
    return this.http.post<LabeledGraphDTO[]>(environment.apiHost + 'property/propertyByDay', 
    {
      id: cityId,
      from: dateFrom,
      to: dateTo,
      measurement: measurment,
    }, options)  
  }
}
  

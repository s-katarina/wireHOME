import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { CityDTO, PropertyDTO, PropertyRequestDTO } from 'src/app/model/model';
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

  constructor(private readonly http: HttpClient) { }

  private selectedProperty = new BehaviorSubject<string>("");
  selectedPropertyId$ = this.selectedProperty.asObservable();

  setSelectedPropertyId(PropertyId: string) {
    this.selectedProperty.next(PropertyId);
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

}

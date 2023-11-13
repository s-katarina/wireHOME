import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { CityDTO, PropertyRequestDTO, PropertyResponseDTO } from 'src/app/model/model';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root'
})
export class PropertyServiceService {

  constructor(private readonly http: HttpClient) { }

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

  getProperties(): Observable<PropertyResponseDTO[]> {
    return this.http.get<PropertyResponseDTO[]>(environment.apiHost + 'property')
  }

}

import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { CityDTO } from 'src/app/model/model';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root'
})
export class PropertyServiceService {

  constructor(private readonly http: HttpClient) { }

  getCities (): Observable<CityDTO[]> {
    return this.http.get<CityDTO[]>(environment.apiHost + 'property/city')
  }

}

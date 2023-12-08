import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { catchError, map, Observable, of } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class MapServiceService {

  constructor(private http: HttpClient) { }

  public getLatLong(address: string): Observable<any> {
    return this.http.get("https://nominatim.openstreetmap.org/search?format=json&q=" + address);
  }
  
  public getAddressFromLatLong(lat: number, lon: number): Observable<any> {
    return this.http.get(
      `https://nominatim.openstreetmap.org/reverse?format=jsonv2&lat=${lat}&lon=${lon}`, 
      {
        headers: new HttpHeaders({
          'accept-language': 'en'
        })
      }
    );
  }

  postRequest(address: string): Observable<any> {

    return this.getLatLong(address).pipe(
      map((res: any) => {
        if (res.length > 0) {
          let ret = {
            address: address,
            latitude: res[0].lat,
            longitude: res[0].lon
          };
          return ret; 
        }
        throw new Error('No results found for the given address.');
      }),
      catchError(error => {
        console.error('Error:', error);
        return of(null); 
      })
    );
  }

}

import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ImageServiceService {

  private imageCache: Map<string, any> = new Map();

  constructor(private http: HttpClient) {}

  uploadPropertyImage(file: File, name:string): Observable<any> {
    const formData: FormData = new FormData();
    formData.append('file', file);
    formData.append('propertyId', name);
    console.log(name)
    return this.http.post<any>(`${environment.apiHost}images/property/upload`, formData);
  }

  uploadDeviceImage(file: File, name:string): Observable<any> {
    const formData: FormData = new FormData();
    formData.append('file', file);
    formData.append('customFileName', name);
    console.log(name)
    return this.http.post<any>(`${environment.apiHost}images/device/upload`, formData);
  }

}



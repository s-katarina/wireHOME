import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ImageServiceService {

  constructor(private http: HttpClient) {}

  uploadPropertyImage(file: File, name:string): Observable<any> {
    const formData: FormData = new FormData();
    formData.append('file', file);
    formData.append('customFileName', name);
    console.log(name)
    return this.http.post<any>(`${environment.apiHost}images/property/upload`, formData);
  }

  getPropertyImage(propertyId: string): Observable<Blob> {
    return this.http.get(`${environment.apiHost}images/property-${propertyId}.jpg`, { responseType: 'blob' });
  }


}



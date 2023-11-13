import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ImageServiceService {

  constructor(private http: HttpClient) {}

  uploadFile(file: File, name:string): Observable<any> {
    const formData: FormData = new FormData();
    formData.append('file', file);
    formData.append('customFileName', name);
    console.log(name)
    return this.http.post<any>(`${environment.apiHost}images/upload`, formData);
  }}

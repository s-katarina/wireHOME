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
    formData.append('customFileName', name);
    console.log(name)
    return this.http.post<any>(`${environment.apiHost}images/property/upload`, formData);
  }

  getPropertyImage(propertyId: string): Observable<Blob> {
    // return this.http.get(`${environment.imgHost}images/property-${propertyId}.jpg`, { responseType: 'blob' });
    return this.http.get(`http://localhost/images/property-1.jpg`, { responseType: 'blob' });
  }

  isImageLoaded(propertyId: string): boolean {
    return this.imageCache.has(propertyId);
  }

  getCachedImage(propertyId: string): any {
    return this.imageCache.get(propertyId);
  }

  cacheImage(propertyId: string, image: any): void {
    this.imageCache.set(propertyId, image);
  }


}



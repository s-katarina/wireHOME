import { Component, OnInit } from '@angular/core';
import { SharingService } from './sharing.service';
import { AuthService } from '../../auth/service/auth.service';
import { SharedPropertyDTO } from 'src/app/model/model';
import { PropertyServiceService } from '../../property/service/property-service.service';
import { ImageServiceService } from '../../service/image-service.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-sharing',
  templateUrl: './sharing.component.html',
  styleUrls: ['./sharing.component.css']
})
export class SharingComponent implements OnInit {

  sharedProperties: SharedPropertyDTO[] = []
  hoveredPropertyId: string | null = null;
  isHovered = false;

  constructor(private sharingService: SharingService, 
              private authService: AuthService, 
              private readonly propertyService: PropertyServiceService,
              public readonly imageService: ImageServiceService,
              private router: Router) { }

  ngOnInit(): void {

    this.sharingService.getSharedWithProperties(this.authService.getId()).subscribe((properties: SharedPropertyDTO[]) => {
      this.sharedProperties = properties
      console.log(this.sharedProperties)
    })

  }

  showImage(propertyId: string): void {
    this.hoveredPropertyId = propertyId;
  }

  hideImage(): void {
    this.hoveredPropertyId = null;
  }


  handleImageError(event: any): void {
    // Handle the image error here, e.g., set a placeholder image
    this.isHovered = false
    event.target.src = '';
    event.target.hidden = true;
  }

  isPropertyHovered(propertyId: string): boolean {
    return this.hoveredPropertyId === propertyId;
  }

  navigateToSingleProperty() {
    const p = this.sharedProperties.find(sharedProperty => sharedProperty.property.id === this.hoveredPropertyId);
    if (p?.property?.propertyStatus != 'PENDING') {
      this.propertyService.setProperty(p?.property);
      this.router.navigate(['/property']);
    }
  }

}

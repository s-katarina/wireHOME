import { Component, OnInit } from '@angular/core';
import { PropertyServiceService } from '../service/property-service.service';
import { PropertyDTO } from 'src/app/model/model';
import { ImageServiceService } from '../../service/image-service.service';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../auth/service/auth.service';

@Component({
  selector: 'app-property-overview',
  templateUrl: './property-overview.component.html',
  styleUrls: ['./property-overview.component.css']
})
export class PropertyOverviewComponent implements OnInit {

  constructor(private readonly propertyService: PropertyServiceService,
              public readonly imageService: ImageServiceService,
              public readonly authService: AuthService
    ) 
    {
    }
    
  properties : PropertyDTO[] = []
  hoveredPropertyId: string | null = null;

  ngOnInit(): void {
    this.propertyService.getProperties().subscribe((res: any) => {
      this.properties = res
      this.loadAllImages()
    })
  }

  isHovered = false;

  showImage(propertyId: string): void {
    this.hoveredPropertyId = propertyId;
  }

  hideImage(): void {
    this.hoveredPropertyId = null;
  }

  private loadAllImages(): void {
    for (const property of this.properties) {
      this.imageService.getPropertyImage(property.id).subscribe(
        (data: any) => {
          const reader = new FileReader();
          reader.onloadend = () => {
            const imageSrc = reader.result;
            this.imageService.cacheImage(property.id, imageSrc);
          };
          reader.readAsDataURL(data);
        },
        (error) => {
          console.error('Error loading image:', error);
        }
      );
    }
  }

  handleImageError(event: any): void {
    // Handle the image error here, e.g., set a placeholder image
    this.isHovered = false
    event.target.src = '';
    event.target.hidden = true;
  }

  getImageSrc(propertyId: string) {
    console.log(this.imageService.getCachedImage(propertyId))
    return this.imageService.getCachedImage(propertyId)
  }

  isPropertyHovered(propertyId: string): boolean {
    return this.hoveredPropertyId === propertyId;
  }


}

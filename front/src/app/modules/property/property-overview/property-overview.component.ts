import { Component, OnInit } from '@angular/core';
import { PropertyServiceService } from '../service/property-service.service';
import { PropertyDTO } from 'src/app/model/model';
import { ImageServiceService } from '../../service/image-service.service';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../../auth/service/auth.service';

@Component({
  selector: 'app-property-overview',
  templateUrl: './property-overview.component.html',
  styleUrls: ['./property-overview.component.css']
})
export class PropertyOverviewComponent implements OnInit {

  constructor(private readonly propertyService: PropertyServiceService,
              public readonly imageService: ImageServiceService,
              public readonly authService: AuthService,
              private router: Router,
              private route: ActivatedRoute
    ) 
    {
    }
    
  properties : PropertyDTO[] = []
  hoveredPropertyId: string | null = null;

  ngOnInit(): void {
    this.propertyService.getProperties().subscribe((res: any) => {
      this.properties = res
    })
  }

  isHovered = false;

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
    const p = this.properties.find(property => property.id === this.hoveredPropertyId);
    if (p?.propertyStatus != 'PENDING') {
      this.propertyService.setProperty(p);
      this.router.navigate(['/property']);
    }
  }

  selectProperty(propertyId: string) {
    this.propertyService.setSelectedPropertyId(propertyId);
  }

}

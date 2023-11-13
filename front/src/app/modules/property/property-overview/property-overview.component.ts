import { Component, OnInit } from '@angular/core';
import { PropertyServiceService } from '../service/property-service.service';
import { PropertyResponseDTO } from 'src/app/model/model';

@Component({
  selector: 'app-property-overview',
  templateUrl: './property-overview.component.html',
  styleUrls: ['./property-overview.component.css']
})
export class PropertyOverviewComponent implements OnInit {

  constructor(private readonly propertyService: PropertyServiceService,
    ) 
    {
      this.propertyService.getProperties().subscribe((res: any) => {
        this.properties = res
      })
     }

  properties : PropertyResponseDTO[] = []

  ngOnInit(): void {
  }

  isHovered = false;

  showImage() {
    this.isHovered = true;
  }

  // Hide the image on mouseout
  hideImage() {
    this.isHovered = false;
  }

}

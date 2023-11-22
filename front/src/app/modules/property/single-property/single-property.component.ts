import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { DeviceDTO, PropertyDTO } from 'src/app/model/model';
import { PropertyServiceService } from '../service/property-service.service';

@Component({
  selector: 'app-single-property',
  templateUrl: './single-property.component.html',
  styleUrls: ['./single-property.component.css']
})
export class SinglePropertyComponent implements OnInit {

  public property: PropertyDTO | undefined;

  appliances: DeviceDTO[] = []
  outdoor: DeviceDTO[] = []
  energyDevices: DeviceDTO[] = []

  constructor(private readonly propertyService: PropertyServiceService,) { 
    this.propertyService.currentProperty.subscribe(
      (property: PropertyDTO | undefined) => (this.property = property)
    );

    this.propertyService.getApliences(this.property?.id || "0").subscribe((res: any) => {
      this.appliances = res;
      console.log(res)
    })

    this.propertyService.getOutdoor(this.property?.id || "0").subscribe((res: any) => {
      this.outdoor = res;
    })

    this.propertyService.getEnergyDevices(this.property?.id || "0").subscribe((res: any) => {
      this.energyDevices = res;
    })
  }


  ngOnInit(): void {
    
  }

  selectProperty(propertyId: string) {
    this.propertyService.setSelectedPropertyId(propertyId);
  }

}

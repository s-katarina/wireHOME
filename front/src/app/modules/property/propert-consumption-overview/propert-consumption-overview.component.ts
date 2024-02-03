import { Component, OnInit } from '@angular/core';
import { PropertyServiceService } from '../service/property-service.service';
import { ByTimeOfDay, LabeledGraphDTO, PropertyDTO, StartEnd } from 'src/app/model/model';
import { CanvasJS } from '@canvasjs/angular-charts';
import { FormControl, FormGroup } from '@angular/forms';

@Component({
  selector: 'app-propert-consumption-overview',
  templateUrl: './propert-consumption-overview.component.html',
  styleUrls: ['./propert-consumption-overview.component.css']
})
export class PropertConsumptionOverviewComponent implements OnInit {


  public property: PropertyDTO | undefined;

  
  constructor(private readonly propertyService: PropertyServiceService) { }

  ngOnInit(): void {
    this.propertyService.currentProperty.subscribe(
      (property: PropertyDTO | undefined) => (this.property = property)
    );
    
  }

}

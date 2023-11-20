import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { PropertyDTO } from 'src/app/model/model';
import { PropertyServiceService } from '../service/property-service.service';

@Component({
  selector: 'app-single-property',
  templateUrl: './single-property.component.html',
  styleUrls: ['./single-property.component.css']
})
export class SinglePropertyComponent implements OnInit {

  public property: PropertyDTO | undefined;

  constructor(private readonly propertyService: PropertyServiceService,) { 
    this.propertyService.currentProperty.subscribe(
      (property: PropertyDTO | undefined) => (this.property = property)
    );
  }


  ngOnInit(): void {
    
  }

}

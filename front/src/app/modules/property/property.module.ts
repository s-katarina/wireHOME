import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MaterialModule } from 'src/app/infrastructure/material/material.module';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { CreatePropertyComponent } from './create-property/create-property.component';
import { AppLayoutModule } from '../layout/layout.module';
import { PropertyOverviewComponent } from './property-overview/property-overview.component';
import { PropertyImageComponent } from './property-image/property-image.component';
import { PropertyRequestsComponent } from './property-requests/property-requests.component';



@NgModule({
  declarations: [
    CreatePropertyComponent,
    PropertyOverviewComponent,
    PropertyImageComponent,
    PropertyRequestsComponent,
  ],
  imports: [
    CommonModule,
    MaterialModule,
    FormsModule,
    ReactiveFormsModule,
    AppLayoutModule
  ]
})
export class PropertyModule { }

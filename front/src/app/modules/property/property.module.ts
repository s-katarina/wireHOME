import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MaterialModule } from 'src/app/infrastructure/material/material.module';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { CreatePropertyComponent } from './create-property/create-property.component';
import { AppLayoutModule } from '../layout/layout.module';
import { PropertyOverviewComponent } from './property-overview/property-overview.component';
import { PropertyRequestsComponent } from './property-requests/property-requests.component';
import { SinglePropertyComponent } from './single-property/single-property.component';
import { AppRoutingModule } from 'src/app/infrastructure/app-routing.module';



@NgModule({
  declarations: [
    CreatePropertyComponent,
    PropertyOverviewComponent,
    PropertyRequestsComponent,
    SinglePropertyComponent,
  ],
  imports: [
    CommonModule,
    MaterialModule,
    FormsModule,
    ReactiveFormsModule,
    AppLayoutModule,
    AppRoutingModule
  ]
})
export class PropertyModule { }

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
import { AdminPropertyOverviewComponent } from './admin-property-overview/admin-property-overview.component';
import { CityOverviewComponent } from './city-overview/city-overview.component';
import { PropertConsumptionOverviewComponent } from './propert-consumption-overview/propert-consumption-overview.component';
import { ReportByDeviceTypeComponent } from './report-by-device-type/report-by-device-type.component';
import { ReportByYearComponent } from './report-by-year/report-by-year.component';
import { ReportByDayComponent } from './report-by-day/report-by-day.component';
import { ReportByWeekComponent } from './report-by-week/report-by-week.component';



@NgModule({
  declarations: [
    CreatePropertyComponent,
    PropertyOverviewComponent,
    PropertyRequestsComponent,
    SinglePropertyComponent,
    AdminPropertyOverviewComponent,
    CityOverviewComponent,
    PropertConsumptionOverviewComponent,
    ReportByDeviceTypeComponent,
    ReportByYearComponent,
    ReportByDayComponent,
    ReportByWeekComponent,
  ],
  imports: [
    CommonModule,
    MaterialModule,
    FormsModule,
    ReactiveFormsModule,
    AppLayoutModule,
    AppRoutingModule
  ],
  exports: [
    ReportByDeviceTypeComponent,
    ReportByDayComponent,
    ReportByWeekComponent,
    ReportByYearComponent
  ]
})
export class PropertyModule {
  
 }

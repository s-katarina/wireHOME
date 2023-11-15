import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LandingComponent } from '../modules/layout/landing/landing.component';
import { CreatePropertyComponent } from '../modules/property/create-property/create-property.component';
import { CreateDeviceComponent } from '../modules/devices/create-device/create-device.component';
import { PropertyOverviewComponent } from '../modules/property/property-overview/property-overview.component';
import { PropertyRequestsComponent } from '../modules/property/property-requests/property-requests.component';

const routes: Routes = [
  { path: 'properties', component: PropertyOverviewComponent },
  { path: 'property-requests', component: PropertyRequestsComponent },
  { path: 'create-property', component: CreatePropertyComponent },
  { path: 'create-device', component: CreateDeviceComponent},
  { path: '**', component: LandingComponent }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }

import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LandingComponent } from '../modules/layout/landing/landing.component';
import { CreatePropertyComponent } from '../modules/property/create-property/create-property.component';
import { PropertyOverviewComponent } from '../modules/property/property-overview/property-overview.component';

const routes: Routes = [
  { path: 'properties', component: PropertyOverviewComponent },
  { path: 'create-property', component: CreatePropertyComponent },
  { path: '**', component: LandingComponent }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }

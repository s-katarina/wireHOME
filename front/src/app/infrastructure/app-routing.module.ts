import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LandingComponent } from '../modules/layout/landing/landing.component';
import { CreatePropertyComponent } from '../modules/property/create-property/create-property.component';
import { PropertyOverviewComponent } from '../modules/property/property-overview/property-overview.component';
import { PropertyRequestsComponent } from '../modules/property/property-requests/property-requests.component';
import { LoginComponent } from '../modules/auth/login/login.component';
import { UnregisteredGuard } from './guard/unregistered.guard';
import { RegisterComponent } from '../modules/auth/register/register.component';

const routes: Routes = [
  { path: 'properties', component: PropertyOverviewComponent },
  { path: 'property-requests', component: PropertyRequestsComponent },
  { path: 'create-property', component: CreatePropertyComponent },
  { path: 'login', component: LoginComponent, canActivate: [UnregisteredGuard] },
  { path: 'register', component: RegisterComponent, canActivate: [UnregisteredGuard] },
  { path: '**', component: LandingComponent }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }

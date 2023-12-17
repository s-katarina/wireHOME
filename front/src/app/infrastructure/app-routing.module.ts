import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LandingComponent } from '../modules/layout/landing/landing.component';
import { CreatePropertyComponent } from '../modules/property/create-property/create-property.component';
import { CreateDeviceComponent } from '../modules/devices/create-device/create-device.component';
import { PropertyOverviewComponent } from '../modules/property/property-overview/property-overview.component';
import { PropertyRequestsComponent } from '../modules/property/property-requests/property-requests.component';
import { SinglePropertyComponent } from '../modules/property/single-property/single-property.component';
import { LoginComponent } from '../modules/auth/login/login.component';
import { UnregisteredGuard } from './guard/unregistered.guard';
import { RegisterComponent } from '../modules/auth/register/register.component';
import { RoleGuard } from './guard/role.guard';
import { TokenGuard } from './guard/token.guard';
import { LampComponent } from '../modules/devices/outdoor/lamp/lamp.component';
import { GateComponent } from '../modules/devices/outdoor/gate/gate.component';
import { EnergyOverviewComponent } from '../modules/devices/large-energy/energy-overview/energy-overview.component';
import { SolarPanelComponent } from '../modules/devices/large-energy/solar-panel/solar-panel.component';
import { BatteryComponent } from '../modules/devices/large-energy/battery/battery.component';

const routes: Routes = [
  { path: 'properties', component: PropertyOverviewComponent, canActivate: [TokenGuard, RoleGuard ], data: {roles: ["AUTH_USER"]}  },
  { path: 'property', component: SinglePropertyComponent, canActivate: [TokenGuard, RoleGuard ], data: {roles: ["AUTH_USER"]}  },
  { path: 'create-property', component: CreatePropertyComponent, canActivate: [TokenGuard, RoleGuard ], data: {roles: ["AUTH_USER"]} },
  { path: 'create-device', component: CreateDeviceComponent, canActivate: [TokenGuard, RoleGuard ], data: {roles: ["AUTH_USER"]} },
  { path: 'login', component: LoginComponent, canActivate: [UnregisteredGuard] },
  { path: 'register', component: RegisterComponent, canActivate: [UnregisteredGuard] },
  { path: 'property-requests', component: PropertyRequestsComponent, canActivate: [TokenGuard, RoleGuard ], data: {roles: ["SUPER_ADMIN", "ADMIN"]}  },
  { path: 'register-admin', component: RegisterComponent, canActivate: [TokenGuard, RoleGuard ], data: {roles: ["SUPER_ADMIN"]} },
  { path: 'lamp', component: LampComponent },
  { path: 'gate', component: GateComponent },
  { path: 'energy-overwiev', component:EnergyOverviewComponent},
  { path: 'solarPanel', component:SolarPanelComponent},
  { path: 'battery', component: BatteryComponent},
  { path: '**', component: LandingComponent }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }

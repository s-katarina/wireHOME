import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { AppRoutingModule } from './infrastructure/app-routing.module';
import { AppComponent } from './app.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';
import { Interceptor } from './infrastructure/interceptor/interceptor.interceptor';
import { MAT_FORM_FIELD_DEFAULT_OPTIONS } from '@angular/material/form-field';
import { MaterialModule } from './infrastructure/material/material.module';
import { AppLayoutModule } from './modules/layout/layout.module';
import { PropertyModule } from './modules/property/property.module';
import { CreateDeviceComponent } from './modules/devices/create-device/create-device.component';
import { RegisterComponent } from './modules/auth/register/register.component';
import { LoginComponent } from './modules/auth/login/login.component';
import { TokenExpirationInterceptor } from './infrastructure/interceptor/token-expiration';
import { LampComponent } from './modules/devices/outdoor/lamp/lamp.component';
import { GateComponent } from './modules/devices/outdoor/gate/gate.component';
import { EnergyOverviewComponent } from './modules/devices/large-energy/energy-overview/energy-overview.component';
import { SolarPanelComponent } from './modules/devices/large-energy/solar-panel/solar-panel.component';

import { CanvasJSAngularChartsModule } from '@canvasjs/angular-charts';
import { DatePipe } from '@angular/common';

@NgModule({
  declarations: [
    AppComponent,
    CreateDeviceComponent,
    LoginComponent,
    RegisterComponent,
    LampComponent,
    GateComponent,
    EnergyOverviewComponent,
    SolarPanelComponent,
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    BrowserAnimationsModule,
    MaterialModule,
    AppLayoutModule,
    PropertyModule,
    ReactiveFormsModule,
    FormsModule,
    HttpClientModule,
    CanvasJSAngularChartsModule
  ],
  providers: [
    {
      provide: MAT_FORM_FIELD_DEFAULT_OPTIONS,
      useValue: {
        appearance: 'outline'
      }
    },
    {
      provide: HTTP_INTERCEPTORS,
      useClass: Interceptor,
      multi: true
    },
    {
      provide: HTTP_INTERCEPTORS,
      useClass: TokenExpirationInterceptor,
      multi: true
    },
    DatePipe
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }

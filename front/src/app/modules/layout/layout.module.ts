import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MaterialModule } from 'src/app/infrastructure/material/material.module';
import { ToolbarComponent } from './toolbar/toolbar.component';
import { LandingComponent } from './landing/landing.component';
import { AppRoutingModule } from 'src/app/infrastructure/app-routing.module';
import { MapComponent } from './map/map.component';


@NgModule({
  declarations: [
    ToolbarComponent,
    LandingComponent,
    MapComponent
  ],
  imports: [
    CommonModule,
    MaterialModule,
    AppRoutingModule
  ],
  exports: [
    ToolbarComponent,
    MapComponent
  ]
})
export class AppLayoutModule { }

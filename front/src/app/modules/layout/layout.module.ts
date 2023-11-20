import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MaterialModule } from 'src/app/infrastructure/material/material.module';
import { ToolbarComponent } from './toolbar/toolbar.component';
import { LandingComponent } from './landing/landing.component';
import { AppRoutingModule } from 'src/app/infrastructure/app-routing.module';
import { MapComponent } from './map/map.component';
import { AdminChangePasswordDialogComponent } from './dialogs/admin-change-password-dialog/admin-change-password-dialog.component';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';


@NgModule({
  declarations: [
    ToolbarComponent,
    LandingComponent,
    MapComponent,
    AdminChangePasswordDialogComponent
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    FormsModule,
    HttpClientModule,
    AppRoutingModule,
    MaterialModule,
    BrowserAnimationsModule
  ],
  exports: [
    ToolbarComponent,
    MapComponent
  ]
})
export class AppLayoutModule { }

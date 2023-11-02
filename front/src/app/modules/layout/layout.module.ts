import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MaterialModule } from 'src/app/infrastructure/material/material.module';
import { ToolbarComponent } from './toolbar/toolbar.component';
import { LandingComponent } from './landing/landing.component';



@NgModule({
  declarations: [
    ToolbarComponent,
    LandingComponent
  ],
  imports: [
    CommonModule,
    MaterialModule
  ],
  exports: [
    ToolbarComponent
  ]
})
export class LayoutModule { }

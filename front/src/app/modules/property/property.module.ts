import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MaterialModule } from 'src/app/infrastructure/material/material.module';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { CreatePropertyComponent } from './create-property/create-property.component';



@NgModule({
  declarations: [
    CreatePropertyComponent
  ],
  imports: [
    CommonModule,
    MaterialModule,
    FormsModule,
    ReactiveFormsModule
  ]
})
export class PropertyModule { }

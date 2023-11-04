import { Component, OnInit } from '@angular/core';
import {FormBuilder, Validators, FormsModule, ReactiveFormsModule, FormControl, AbstractControl} from '@angular/forms';
import { map, Observable, startWith } from 'rxjs';
import { PropertyServiceService } from '../service/property-service.service';
@Component({
  selector: 'app-create-property',
  templateUrl: './create-property.component.html',
  styleUrls: ['./create-property.component.css']
})
export class CreatePropertyComponent implements OnInit {

  firstFormGroup = this._formBuilder.group({
    floorCount: ['', [Validators.required, Validators.max(100), Validators.min(0)]],
    area: ['', [Validators.required, Validators.max(10000), Validators.min(0)]]
  });
  secondFormGroup = this._formBuilder.group({
    address: ['', Validators.required],
  });
  isLinear = true;
  
  constructor(private _formBuilder: FormBuilder,
    private readonly propertyService: PropertyServiceService) {
    this.filteredChoices = this.selectedCityControl.valueChanges.pipe(
      startWith(''),
      map(value => this.filterChoices(value!))
    );
    this.propertyService.getCities().subscribe((res: any) => {
      this.cityChoices = res;
      console.log(res);
      console.log(this.cityChoices);
    })
   }

  ngOnInit(): void {
  }

  panelOpenCondition = false
  selectedToggleVal: string = '';

  public onToggleValChange(val: string) {
    this.selectedToggleVal = val;
    this.panelOpenCondition = true
  }

  isStepEditable = true;

  cityChoices = ['Option 1', "2", "3"];
  selectedCity: string = "";
  selectedCityControl = new FormControl(this.cityChoices[0]);
  filteredChoices: Observable<string[]>;


  onCitySelectionChange(event: any): void {
    if (this.cityChoices.includes(event.option.value)) {
      this.selectedCity = event.option.value;
    } 
  }

  filterChoices(value: string): string[] {
    const filterValue = value.toLowerCase();
    return this.cityChoices.filter(option => option.toLowerCase().includes(filterValue));
  }
  
  onInputBlur(): void {
    const inputValue = this.selectedCityControl.value;
    if (inputValue && !this.cityChoices.includes(inputValue)) {
      this.selectedCityControl.setValue(''); // Clear the input field if the value is not in the choices
    }
  }

  send() {
    this.isStepEditable = false;
  }
  

}

function maxValueValidator(maxValue: number): Validators {
  return (control: AbstractControl): { [key: string]: any } | null => {
    const isValid = control.value <= maxValue;
    return isValid ? null : { 'maxValue': { value: control.value } };
  };
}

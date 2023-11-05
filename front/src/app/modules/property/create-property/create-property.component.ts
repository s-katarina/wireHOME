import { Component, OnInit } from '@angular/core';
import {FormBuilder, Validators, FormsModule, ReactiveFormsModule, FormControl, AbstractControl} from '@angular/forms';
import { map, Observable, startWith } from 'rxjs';
import { CityDTO, PropertyRequestDTO } from 'src/app/model/model';
import { PropertyServiceService } from '../service/property-service.service';
@Component({
  selector: 'app-create-property',
  templateUrl: './create-property.component.html',
  styleUrls: ['./create-property.component.css']
})
export class CreatePropertyComponent implements OnInit {

  panelOpenCondition = false
  selectedToggleTypeVal: string = '';

  isStepEditable = true;

  cityChoices : CityDTO[] = [];
  selectedCityControl = new FormControl<string | CityDTO>('');
  filteredChoices: Observable<CityDTO[]>;
  selectedCity : CityDTO | null = null;

  firstFormGroup = this._formBuilder.group({
    floorCount: ['', [Validators.required, Validators.max(100), Validators.min(0)]],
    area: ['', [Validators.required, Validators.max(10000), Validators.min(0)]]
  });
  secondFormGroup = this._formBuilder.group({
    address: ['', Validators.required],
    city: ['', Validators.required]
  });
  isLinear = true;
  
  constructor(private _formBuilder: FormBuilder,
    private readonly propertyService: PropertyServiceService) { 

      this.propertyService.getCities().subscribe((res: any) => {
        this.cityChoices = res;
      })
  
      this.filteredChoices = this.selectedCityControl.valueChanges.pipe(
        startWith(''),
        map(value => {
          const name = typeof value === 'string' ? value : value?.name;
          return name ? this.filterChoices(name as string) : this.cityChoices.slice();
        }),
      );
    }

  ngOnInit(): void {

  }


  public onToggleValChange(val: string) {
    this.selectedToggleTypeVal = val;
    this.panelOpenCondition = true
  }


  filterChoices(name: string): CityDTO[] {
    const filterValue = name.toLowerCase();
    return this.cityChoices.filter(option => option.name.toLowerCase().includes(filterValue));
  }

  displayFn(city : CityDTO): string {
    return city && city.name ? city.name : '';
  }

  send() {
    const areaValue = this.firstFormGroup.get('area')?.value;
    const floorCountValue = this.firstFormGroup.get('floorCount')?.value;

    const dto : PropertyRequestDTO  = {
      propertyType: this.selectedToggleTypeVal,
      address: this.secondFormGroup.get('address')?.value ?? '',
      cityId: this.selectedCity?.id ?? -1,
      area: areaValue != null && areaValue != undefined ? parseFloat(areaValue) ?? 0 : null,
      floorCount:  floorCountValue != null && floorCountValue != undefined ? parseInt(floorCountValue) ?? 0 : null,
  };


    this.propertyService.create(dto).subscribe((res: any) => {
    });

    this.isStepEditable = false;
    this.firstFormGroup.reset();
    this.secondFormGroup.reset();
  }

  onCitySelectionChange(event: any): void {
    if (this.cityChoices.includes(event.option.value)) {
      this.selectedCity = event.option.value;
      this.secondFormGroup.get('city')?.setValue(event.option.value);
    } 
  }

  onCityInputChanged(_: any): void {
    this.selectedCity = null;
  }

}


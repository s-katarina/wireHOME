import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';

@Component({
  selector: 'app-create-device',
  templateUrl: './create-device.component.html',
  styleUrls: ['./create-device.component.css']
})
export class CreateDeviceComponent implements OnInit {
  selectedValue: string = 'autonomn'
  selectedType:string = ''
  regimes:string[] = ["llalal", "sdfsdfdxf", "llalal", "sdfsdfdxf","llalal", "sdfsdfdxf","llalal", "sdfsdfdxf","llalal", "sdfsdfdxf"]
  commonSection:boolean = false
  regimeSection:boolean = false
  klimaSection:boolean = false
  batterySection:boolean = false
  pannelSection:boolean = false
  chargerSection:boolean = false

  firstFormGroup = this._formBuilder.group({
    consuption: [{ value: '', disabled: true }, [Validators.required, Validators.max(1000000), Validators.min(0)]],
  });

  modelFormGroup = this._formBuilder.group({
    name: ['', [Validators.required]],
  });

  airConditionerFormGroup = this._formBuilder.group({
    maxTemp: ['', [Validators.required, Validators.max(50), Validators.min(0)]],
    minTemp: ['', [Validators.required, Validators.max(50), Validators.min(0)]],
  });

  panelFormGroup = this._formBuilder.group({
    efficiency: ['', [Validators.required, Validators.max(100), Validators.min(0)]],
    surface: ['', [Validators.required, Validators.max(10000), Validators.min(0)]],
  });

  bateryFormGroup = this._formBuilder.group({
    capacity: ['', [Validators.required, Validators.max(1000000), Validators.min(0)]],
  });

  chargerFormGroup = this._formBuilder.group({
    pluginNum: ['', [Validators.required, Validators.max(100), Validators.min(0)]],
    capacity: ['', [Validators.required, Validators.max(10000), Validators.min(0)]],
  });

  constructor(private _formBuilder: FormBuilder) { }

  ngOnInit(): void {
  }

  updateFieldState() {  //malo visak funkcija ali nema veye
    if (this.selectedValue === 'autonomn'){
      this.firstFormGroup.get("consuption")?.disable()
    } else {
      this.firstFormGroup.get("consuption")?.enable()
    }
    console.log(this.selectedValue)
  }

  updateDeviceType() {
    this.commonSection = true
    this.regimeSection = false
    this.klimaSection = false
    this.batterySection = false
    this.pannelSection = false
    this.chargerSection = false
    if(this.selectedType==="airConditioner" || this.selectedType==="washingMashine"){
      this.regimeSection = true
    }
    if(this.selectedType==="airConditioner"){
      this.klimaSection = true
    }
    if(this.selectedType==="solarPanel"){
      this.pannelSection = true
    }
    if(this.selectedType==="houseBatery"){
      this.batterySection = true
    }
    if(this.selectedType==="charger"){
      this.chargerSection = true
    }
  }

  register(){
    
  }

}

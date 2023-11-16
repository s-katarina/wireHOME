import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { DeviceCreationService } from '../service/device-creation.service';
import { DeviceRequestDTO } from 'src/app/model/model';
@Component({
  selector: 'app-create-device',
  templateUrl: './create-device.component.html',
  styleUrls: ['./create-device.component.css']
})
export class CreateDeviceComponent implements OnInit {
  selectedValue: string = 'autonomn'
  selectedType:string = ''
  selectedRegimes:string[] = []
  regimes:string[] = ["llalal", "sdfsdfdxf", "llalal", "sdfsdfdxf","llalal", "sdfsdfdxf","llalal", "sdfsdfdxf","llalal", "sdfsdfdxf"]
  commonSection:boolean = false
  regimeSection:boolean = false
  klimaSection:boolean = false
  batterySection:boolean = false
  pannelSection:boolean = false
  chargerSection:boolean = false

  dictionary: {[deviceType:string]: Function } = {}
//////////////REFAKTORISI SVAKI UREDJAJ U SVOJU COMPONENTU STO NISI RAZMISLILA
////////BILO BI 5 PUTA BOLJE I 5 PUTA VISE KOMPONENTI ALI NEMA VEZE
  firstFormGroup = this._formBuilder.group({
    consuption: [{ value: 0, disabled: true }, [Validators.required, Validators.max(1000000), Validators.min(0)]],
  });

  modelFormGroup = this._formBuilder.group({
    name: ['', [Validators.required]],
  });

  airConditionerFormGroup = this._formBuilder.group({
    maxTemp: [0, [Validators.required, Validators.max(50), Validators.min(0)]],
    minTemp: [0, [Validators.required, Validators.max(50), Validators.min(0)]],
  });

  panelFormGroup = this._formBuilder.group({
    efficiency: [0, [Validators.required, Validators.max(100), Validators.min(0)]],
    surface: [0, [Validators.required, Validators.max(10000), Validators.min(0)]],
  });

  bateryFormGroup = this._formBuilder.group({
    capacity: [0, [Validators.required, Validators.max(1000000), Validators.min(0)]],
  });

  chargerFormGroup = this._formBuilder.group({
    pluginNum: [0, [Validators.required, Validators.max(100), Validators.min(0)]],
    capacity: [0, [Validators.required, Validators.max(10000), Validators.min(0)]],
  });

  constructor(private _formBuilder: FormBuilder,
    private readonly deviceService: DeviceCreationService) { 
      this.deviceService.getRegimes().subscribe((res: any) => {
        this.regimes = res;
      })
    }

  ngOnInit(): void {
  }

  onCheckboxChange(event: Event) {
    // Access the checkbox value
    const isChecked = (event.target as HTMLInputElement).checked;
    const value = (event.target as HTMLInputElement).value
    // Perform your action based on the checkbox state
    if (isChecked) {
      console.log('Checkbox is checked!');
      this.selectedRegimes.concat(value)
      // Add any logic or function calls you need here
    } else {
      console.log('Checkbox is unchecked!');
      this.selectedRegimes.filter(item => item !== value);
      // Add logic for when the checkbox is unchecked
    }
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
    let device: DeviceRequestDTO = {
      modelName: this.modelFormGroup.get("name")?.value ?? "",
      usesElectricity: this.selectedValue === 'house',
      consumptionAmount: this.firstFormGroup.get("consuption")?.value ?? 0,
      propertyId: 1,
      regimes: this.selectedRegimes,
      minTemp: this.airConditionerFormGroup.get("minTemp")?.value ?? 0,
      maxTemp: this.airConditionerFormGroup.get("maxTemp")?.value ?? 0,
      panelSize: this.panelFormGroup.get("surface")?.value ?? 0,
      efficiency: this.panelFormGroup.get("efficiency")?.value ?? 0,
      capacity: this.chargerFormGroup.get("capacity")?.value ?? 0,
      portNumber: this.chargerFormGroup.get("pluginNum")?.value ?? 0,
    }
    if(!device.usesElectricity){
      device.consumptionAmount = 0
    }

    if(this.selectedType==="ambientSensore"){
      this.deviceService.createAmbientalSensor(device).subscribe((res: any) => {
        console.log(res);
      })
    } 
    else if (this.selectedType==="airConditioner")
    {
      this.deviceService.createAirConditioner(device).subscribe((res: any) => {
        console.log(res);
      })
    }
    else if (this.selectedType==="washingMashine")
    {
      this.deviceService.createWashingMachine(device).subscribe((res: any) => {
        console.log(res);
      })
    }
    else if (this.selectedType==="lamp")
    {
      this.deviceService.createLamp(device).subscribe((res: any) => {
        console.log(res);
      })
    }
    else if (this.selectedType==="gate")
    {
      this.deviceService.createGate(device).subscribe((res: any) => {
        console.log(res);
      })
    }
    else if (this.selectedType==="sprinklers")
    {
      this.deviceService.createSprinkler(device).subscribe((res: any) => {
        console.log(res);
      })
    }
    else if (this.selectedType==="solarPanel")
    {
      this.deviceService.createSolarPanel(device).subscribe((res: any) => {
        console.log(res);
      })
    }
    else if (this.selectedType==="houseBatery")
    {
      device.capacity = this.bateryFormGroup.get("capacity")?.value ?? 0
      this.deviceService.createBattery(device).subscribe((res: any) => {
        console.log(res);
      })
    }
    else
    {
      this.deviceService.createCharger(device).subscribe((res: any) => {
        console.log(res);
      })
    }
  }

}

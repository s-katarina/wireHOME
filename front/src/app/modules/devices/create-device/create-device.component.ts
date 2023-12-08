import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { DeviceCreationService } from '../service/device-creation.service';
import { DeviceRequestDTO } from 'src/app/model/model';
import { PropertyServiceService } from '../../property/service/property-service.service';
import { ImageServiceService } from '../../service/image-service.service';
import Swal from 'sweetalert2';
import { MatStepper } from '@angular/material/stepper';
@Component({
  selector: 'app-create-device',
  templateUrl: './create-device.component.html',
  styleUrls: ['./create-device.component.css']
})
export class CreateDeviceComponent implements OnInit {

  propertyId:string = "" // For which device is added
  
  selectedType:string | null = null   // Device type

  selectedToggleValue: string = 'HOUSE'   // HOUSE or AUTONOMN

  selectedRegimesAirConditioner:string[] = []
  selectedRegimesWashingMachine:string[] = []
  airConditionerRegimes:string[] = []
  washingMachineRegimes:string[] = []

  // Determine form fields for type of device
  commonSection:boolean = false
  airCondRegimeSection:boolean = false
  washingMashRegimeSection:boolean = false
  batterySection:boolean = false
  pannelSection:boolean = false
  chargerSection:boolean = false

  dictionary: {[deviceType:string]: Function } = {}

  // Maps device type to device display name in mat second step
  deviceDisplayNameMap : {[deviceType:string]: string} = {
    'ambientSensore': "Ambient sensor",
    'airConditioner': "Air conditioner",
    'washingMashine': "Washing machine",
    'lamp': "Lamp",
    'gate': "Gate",
    'sprinklers': "Sprinklers",
    'solarPanel': "Solar panel",
    'houseBatery': "House battery",
    'charger': "Charger"
  }

  firstFormGroup = this._formBuilder.group({
  });

  secondFormGroup = this._formBuilder.group({
    name: ['', [Validators.required]],
    consuption: [{ value: 0, disabled: true }, [Validators.required, Validators.max(1000000), Validators.min(0)]],
    airConditioner: this._formBuilder.group({
      maxTemp: [0, [Validators.max(60), Validators.min(0)]],
      minTemp: [0, [Validators.max(60), Validators.min(0)]],
    }),
    panel: this._formBuilder.group({
      efficiency: [0, [Validators.max(100), Validators.min(0)]],
      surface: [0, [Validators.max(10000), Validators.min(0)]],
    }),
    battery: this._formBuilder.group({
      capacity: [0, [Validators.max(1000000), Validators.min(0)]],
    }),
    charger: this._formBuilder.group({
      pluginNum: [0, [Validators.max(100), Validators.min(0)]],
      capacity: [0, [Validators.max(1000000), Validators.min(0)]],
    })
  });

  success : boolean = false;  // Success of creation after register device is clicked


  @ViewChild('fileInput') fileInput: ElementRef<HTMLInputElement> | undefined;
  imgURL: string | ArrayBuffer | null = null;   // For displaying image after upload is clicked
  img: File | null = null;


  constructor(private _formBuilder: FormBuilder,
    private readonly deviceService: DeviceCreationService,
    private readonly propertyService: PropertyServiceService,
    private readonly imageService: ImageServiceService) {

      this.deviceService.getAirConditionerRegimes().subscribe((res: any) => {
        this.airConditionerRegimes = res;
      })

      this.deviceService.getWashingMachineRegimes().subscribe((res: any) => {
        this.washingMachineRegimes = res;
      })
    }

  ngOnInit(): void {
    this.propertyService.selectedPropertyId$.subscribe((propertyId) => {
      this.propertyId = propertyId;
    });
  }

  onCheckboxChange(event: Event) {
    const isChecked = (event.target as HTMLInputElement).checked;
    const value = (event.target as HTMLInputElement).value
    if (isChecked) {
      if (this.washingMashRegimeSection) {
        this.selectedRegimesWashingMachine.push(value)
      } else{
        this.selectedRegimesAirConditioner.push(value)


      }
    } else {
      if (this.washingMashRegimeSection) {
        this.selectedRegimesWashingMachine = this.selectedRegimesWashingMachine.filter(item => item !== value);
      } else{
        this.selectedRegimesAirConditioner = this.selectedRegimesAirConditioner.filter(item => item !== value);
      }
    }

  }

  // On device type selection change
  updateDeviceType(event: any) {
    console.log(event)
    this.selectedType = event.value;
    this.commonSection = true
    this.airCondRegimeSection = false
    this.washingMashRegimeSection = false
    this.batterySection = false
    this.pannelSection = false
    this.chargerSection = false
    switch (this.selectedType) {
      case "airConditioner":
        this.airCondRegimeSection = true;
        break;
      case "washingMashine":
        this.washingMashRegimeSection = true;
        break;
      case "solarPanel":
        this.pannelSection = true;
        break;
      case "houseBatery":
        this.batterySection = true;
        break;
      case "charger":
        this.chargerSection = true;
        break;
      default:
        break;
    }
  }

 

  openFileInput(): void {
    if (this.fileInput) {
      this.fileInput.nativeElement.click();
    }
  }

  async onFileSelected(event: any): Promise<void> {
    
    const fileInput = event.target as HTMLInputElement;
    
    if (fileInput.files && fileInput.files.length > 0) {
      this.img = fileInput.files[0];
      

      const mimeType = this.img.type;
      if (mimeType.match(/image\/*/) == null) {
          alert("Only images are supported.");
          return;
      }


      const reader = new FileReader();
      reader.readAsDataURL(this.img);
      reader.onload = (_event) => {
        this.imgURL = reader.result;
      }
    }
  }
  
  
  deleteImage(): void {
    this.imgURL = null
    this.img = null
  }

  onToggleValChange(type: string) {
    console.log(type);
    this.selectedToggleValue = type;
    if (type === 'HOUSE') this.secondFormGroup.controls.consuption.enable();
    else this.secondFormGroup.controls.consuption.disable();
  }

  isDataValid(): boolean {
    if (this.imgURL == null ||
      this.secondFormGroup.invalid) return false;
    return true;
  }

  register() {

    let device: DeviceRequestDTO = {
      modelName: this.secondFormGroup.get("name")?.value ?? "",
      usesElectricity: this.selectedToggleValue === 'HOUSE',
      consumptionAmount: this.secondFormGroup.get("consuption")?.value ?? 0,
      propertyId: this.propertyId,
      regimes: [],
      minTemp: this.secondFormGroup.get("airConditioner.minTemp")?.value ?? 0,
      maxTemp: this.secondFormGroup.get("airConditioner.maxTemp")?.value ?? 0,
      panelSize: this.secondFormGroup.get("panel.surface")?.value ?? 0,
      efficiency: this.secondFormGroup.get("panel.efficiency")?.value ?? 0,
      capacity: this.secondFormGroup.get("charger.capacity")?.value ?? 0,
      portNumber: this.secondFormGroup.get("charger.pluginNum")?.value ?? 0,
    }
    if(!device.usesElectricity){
      device.consumptionAmount = 0
    }

    console.log(device);

    this.deviceService.createDevice(device, this.selectedType!,  
      this.selectedRegimesWashingMachine, this.selectedRegimesAirConditioner, 
     this.secondFormGroup.get("battery.capacity")?.value ?? 0,).subscribe((res: any) => {
      let resObj = JSON.parse(res);
      console.log(resObj);
      if ( resObj.status != 200 ) {
        console.log("greska");
        this.fireSwalToast(false, "Oops. Something went wrong.");
        return
      }
      this.imageService.uploadDeviceImage(this.img!, resObj.data.id).subscribe((res: any) => {
        console.log(res);
        this.fireSwalToast(true, "Device created.");
        this.success = true;

      }, (err: any) => {
        console.log("propao upload", err);
        this.fireSwalToast(false, "Oops. Image couldn't be saved.");

      });
    }, (err: any) => {
      console.log("pogresno popunjeni podati", err);
      this.fireSwalToast(false, "Oops. Check your data.");
    })
  }

  private fireSwalToast(success: boolean, title: string): void {
    const Toast = Swal.mixin({
      toast: true,
      position: 'top-end',
      showConfirmButton: false,
      timer: 3000,
      timerProgressBar: true,
      didOpen: (toast) => {
        toast.addEventListener('mouseenter', Swal.stopTimer)
        toast.addEventListener('mouseleave', Swal.resumeTimer)
      }
    })
    
    Toast.fire({
      icon: success ? 'success' : 'error',
      title: title
    })
  }


}

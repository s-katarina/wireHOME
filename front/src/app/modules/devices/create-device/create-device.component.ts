import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { DeviceCreationService } from '../service/device-creation.service';
import { DeviceRequestDTO } from 'src/app/model/model';
import { PropertyServiceService } from '../../property/service/property-service.service';
import { ImageServiceService } from '../../service/image-service.service';
import Swal from 'sweetalert2';
@Component({
  selector: 'app-create-device',
  templateUrl: './create-device.component.html',
  styleUrls: ['./create-device.component.css']
})
export class CreateDeviceComponent implements OnInit {
  selectedValue: string = 'autonomn'
  selectedType:string = ''
  selectedRegimesAirConditioner:string[] = []
  selectedRegimesWashingMachine:string[] = []
  propertyId:string = ""
  imageName:string = ""
  airConditionerRegimes:string[] = []
  washingMachineRegimes:string[] = []
  commonSection:boolean = false
  airCondRegimeSection:boolean = false
  washingMashRegimeSection:boolean = false
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

  @ViewChild('fileInput') fileInput: ElementRef<HTMLInputElement> | undefined;
  // imgString: string = ''
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
      // Fetch and display shoes for the selected shop here
    });
  }

  onCheckboxChange(event: Event) {
    // Access the checkbox value
    const isChecked = (event.target as HTMLInputElement).checked;
    const value = (event.target as HTMLInputElement).value
    // Perform your action based on the checkbox state
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

  updateFieldState() {  //malo visak funkcija ali nema veye
    if (this.selectedValue === 'autonomn'){
      this.firstFormGroup.get("consuption")?.disable()
    } else {
      this.firstFormGroup.get("consuption")?.enable()
    }
  }

  updateDeviceType() {
    this.commonSection = true
    this.airCondRegimeSection = false
    this.washingMashRegimeSection = false
    this.batterySection = false
    this.pannelSection = false
    this.chargerSection = false
    if(this.selectedType==="airConditioner"){
      this.airCondRegimeSection = true
    }
    else if(this.selectedType==="washingMashine"){
      this.washingMashRegimeSection = true
    }
    else if(this.selectedType==="solarPanel"){
      this.pannelSection = true
    }
    else if(this.selectedType==="houseBatery"){
      this.batterySection = true
    }
    else if (this.selectedType==="charger"){
      this.chargerSection = true
    }
  }

  register(){
    let device: DeviceRequestDTO = {
      modelName: this.modelFormGroup.get("name")?.value ?? "",
      usesElectricity: this.selectedValue === 'house',
      consumptionAmount: this.firstFormGroup.get("consuption")?.value ?? 0,
      propertyId: this.propertyId,
      regimes: [],
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
        let resObj = JSON.parse(res);
        console.log(resObj);
        if ( resObj.status != 200 ) {
          console.log("greska")
          return
        }
        this.imageService.uploadDeviceImage(this.img!, resObj.data.id).subscribe((res: any) => {
          console.log(res);
          this.fireSwalToast(true, "Device created.")

          // this.firstFormGroup.reset();
        }, (err: any) => {
          console.log("propao upload", err)
          this.fireSwalToast(false, "Oops. Image couldn't be saved.")

        });
      }, (err: any) => {
        console.log("pogresno popunjeni podati", err)
        this.fireSwalToast(false, "Oops. Check your data.")
      })
    } 
    else if (this.selectedType==="airConditioner")
    {
      device.regimes = this.selectedRegimesAirConditioner
      this.deviceService.createAirConditioner(device).subscribe((res: any) => {
        let resObj = JSON.parse(res);
        console.log(resObj);
        if ( resObj.status != 200 ) {
          console.log("greska")
          return
        }
        this.imageService.uploadDeviceImage(this.img!, resObj.data.id).subscribe((res: any) => {
          console.log(res);
          this.fireSwalToast(true, "Device created.")

          // this.firstFormGroup.reset();
        }, (err: any) => {
          console.log("propao upload", err)
          this.fireSwalToast(false, "Oops. Image couldn't be saved.")

        });
      }, (err: any) => {
        console.log("pogresno popunjeni podati", err)
        this.fireSwalToast(false, "Oops. Check your data.")
      })
    }
    else if (this.selectedType==="washingMashine")
    {
      device.regimes = this.selectedRegimesWashingMachine
      this.deviceService.createWashingMachine(device).subscribe((res: any) => {
        let resObj = JSON.parse(res);
        console.log(resObj);
        if ( resObj.status != 200 ) {
          console.log("greska")
          return
        }
        this.imageService.uploadDeviceImage(this.img!, resObj.data.id).subscribe((res: any) => {
          console.log(res);
          this.fireSwalToast(true, "Device created.")

          // this.firstFormGroup.reset();
        }, (err: any) => {
          console.log("propao upload", err)
          this.fireSwalToast(false, "Oops. Image couldn't be saved.")

        });
      }, (err: any) => {
        console.log("pogresno popunjeni podati", err)
        this.fireSwalToast(false, "Oops. Check your data.")
      })
    }
    else if (this.selectedType==="lamp")
    {
      this.deviceService.createLamp(device).subscribe((res: any) => {
        let resObj = JSON.parse(res);
        console.log(resObj);
        if ( resObj.status != 200 ) {
          console.log("greska")
          return
        }
        this.imageService.uploadDeviceImage(this.img!, resObj.data.id).subscribe((res: any) => {
          console.log(res);
          this.fireSwalToast(true, "Device created.")

          // this.firstFormGroup.reset();
        }, (err: any) => {
          console.log("propao upload", err)
          this.fireSwalToast(false, "Oops. Image couldn't be saved.")

        });
      }, (err: any) => {
        console.log("pogresno popunjeni podati", err)
        this.fireSwalToast(false, "Oops. Check your data.")
      })
    }
    else if (this.selectedType==="gate")
    {
      this.deviceService.createGate(device).subscribe((res: any) => {
        let resObj = JSON.parse(res);
        console.log(resObj);
        if ( resObj.status != 200 ) {
          console.log("greska")
          return
        }
        this.imageService.uploadDeviceImage(this.img!, resObj.data.id).subscribe((res: any) => {
          console.log(res);
          this.fireSwalToast(true, "Device created.")

          // this.firstFormGroup.reset();
        }, (err: any) => {
          console.log("propao upload", err)
          this.fireSwalToast(false, "Oops. Image couldn't be saved.")

        });
      }, (err: any) => {
        console.log("pogresno popunjeni podati", err)
        this.fireSwalToast(false, "Oops. Check your data.")
      })
    }
    else if (this.selectedType==="sprinklers")
    {
      this.deviceService.createSprinkler(device).subscribe((res: any) => {
        let resObj = JSON.parse(res);
        console.log(resObj);
        if ( resObj.status != 200 ) {
          console.log("greska")
          return
        }
        this.imageService.uploadDeviceImage(this.img!, resObj.data.id).subscribe((res: any) => {
          console.log(res);
          this.fireSwalToast(true, "Device created.")

          // this.firstFormGroup.reset();
        }, (err: any) => {
          console.log("propao upload", err)
          this.fireSwalToast(false, "Oops. Image couldn't be saved.")

        });
      }, (err: any) => {
        console.log("pogresno popunjeni podati", err)
        this.fireSwalToast(false, "Oops. Check your data.")
      })
    }
    else if (this.selectedType==="solarPanel")
    {
      this.deviceService.createSolarPanel(device).subscribe((res: any) => {
        let resObj = JSON.parse(res);
        console.log(resObj);
        if ( resObj.status != 200 ) {
          console.log("greska")
          return
        }
        this.imageService.uploadDeviceImage(this.img!, resObj.data.id).subscribe((res: any) => {
          console.log(res);
          this.fireSwalToast(true, "Device created.")

          // this.firstFormGroup.reset();
        }, (err: any) => {
          console.log("propao upload", err)
          this.fireSwalToast(false, "Oops. Image couldn't be saved.")

        });
      }, (err: any) => {
        console.log("pogresno popunjeni podati", err)
        this.fireSwalToast(false, "Oops. Check your data.")
      })
    }
    else if (this.selectedType==="houseBatery")
    {
      device.capacity = this.bateryFormGroup.get("capacity")?.value ?? 0
      this.deviceService.createBattery(device).subscribe((res: any) => {
        let resObj = JSON.parse(res);
        console.log(resObj);
        if ( resObj.status != 200 ) {
          console.log("greska")
          return
        }
        this.imageService.uploadDeviceImage(this.img!, resObj.data.id).subscribe((res: any) => {
          console.log(res);
          this.fireSwalToast(true, "Device created.")

          // this.firstFormGroup.reset();
        }, (err: any) => {
          console.log("propao upload", err)
          this.fireSwalToast(false, "Oops. Image couldn't be saved.")

        });
      }, (err: any) => {
        console.log("pogresno popunjeni podati", err)
        this.fireSwalToast(false, "Oops. Check your data.")
      })
    }
    else
    {
      this.deviceService.createCharger(device).subscribe((res: any) => {
        let resObj = JSON.parse(res);
        console.log(resObj);
        if ( resObj.status != 200 ) {
          console.log("greska")
          return
        }
        this.imageService.uploadDeviceImage(this.img!, resObj.data.id).subscribe((res: any) => {
          console.log(res);
          this.fireSwalToast(true, "Device created.")

          // this.firstFormGroup.reset();
        }, (err: any) => {
          console.log("propao upload", err)
          this.fireSwalToast(false, "Oops. Image couldn't be saved.")

        });
      }, (err: any) => {
        console.log("pogresno popunjeni podati", err)
        this.fireSwalToast(false, "Oops. Check your data.")
      })
    }

    console.log(device)
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
        console.log(this.imgURL)
        this.imageName = this.img?.name || ""
      }
      // if (this.img) {
      //   this.imgString = String(await this.toBase64(img));

      // };
    }
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

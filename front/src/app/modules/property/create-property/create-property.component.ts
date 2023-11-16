import { AfterViewInit, Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import {FormBuilder, Validators, FormsModule, ReactiveFormsModule, FormControl, AbstractControl} from '@angular/forms';
import { map, Observable, startWith } from 'rxjs';
import { CityDTO, PropertyRequestDTO } from 'src/app/model/model';
import { PropertyServiceService } from '../service/property-service.service';
import { MapComponent } from '../../layout/map/map.component';
import { MapServiceService } from '../../layout/service/map-service.service';
import { ImageServiceService } from '../../service/image-service.service';

@Component({
  selector: 'app-create-property',
  templateUrl: './create-property.component.html',
  styleUrls: ['./create-property.component.css']
})
export class CreatePropertyComponent implements OnInit, AfterViewInit {

  // @ViewChild(MapComponent, {static : true}) map : MapComponent | undefined;

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
  
  @ViewChild(MapComponent, {static : true}) map : MapComponent | undefined;

  success : boolean = true;

  @ViewChild('fileInput') fileInput: ElementRef<HTMLInputElement> | undefined;
  // imgString: string = ''
  imgURL: string | ArrayBuffer | null = null;   // For displaying image after upload is clicked
  img: File | null = null;

  constructor(private _formBuilder: FormBuilder,
    private readonly propertyService: PropertyServiceService,
    private readonly mapService: MapServiceService,
    private readonly imageService: ImageServiceService) { 

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
      let resObj = JSON.parse(res);
      console.log(resObj);
      if ( resObj.status != 200 ) {
        this.success = false
        return
      }
      this.imageService.uploadPropertyImage(this.img!, resObj.data.id).subscribe((res: any) => {
        console.log(res);
        this.isStepEditable = false;
        this.firstFormGroup.reset();
        this.secondFormGroup.reset();
        this.deleteImage();
      }, (err: any) => {
        console.log("Img", err)
        this.success = true
      });
    }, (err: any) => {
      console.log("Upload", err)
      this.success = false
    });

  }

  onCitySelectionChange(event: any): void {
    if (this.cityChoices.includes(event.option.value)) {
      this.selectedCity = event.option.value;
      this.secondFormGroup.get('city')?.setValue(event.option.value);
    } 
  }

  onAddressInputChanged(): void {
    this.findPinFromAddress();
  }

  onCityInputChanged(_: any): void {
    this.selectedCity = null;
    this.map?.removeMarker();
  }

  ngAfterViewInit (): void {
    // this.role = this.authService.getRole()
    setTimeout(() => {
      this.registerOnClick()
    }, 1000)
  }

  registerOnClick(): void {
    this.map?.getMap().on('click', (e: any) => {
      
      const coord = e.latlng;
      const lat = coord.lat;
      const lng = coord.lng;
      this.mapService.getAddressFromLatLong(lat, lng).subscribe((res) => {

        if (res.address !== undefined) {
          let street = res.address.road;
          let houseNumber = res.address.house_number ? " " + res.address.house_number : "";
          let city = res.address.city;
          let displayAddress = `${street}${houseNumber}`

          if (street === undefined) {
            this.map?.marker.bindPopup('Address is not valid since it doesn\'t belong to a city.').openPopup();
          } else {
            this.secondFormGroup.get('address')?.setValue(displayAddress);
            this.secondFormGroup.get('city')?.setValue(city);
            this.map?.marker.bindPopup(displayAddress).openPopup();
          }
        } else {
          this.map?.marker.bindPopup('Address is not valid since it doesn\'t belong to a city.').openPopup();
        }

        

      })
    })
  }

  findPinFromAddress() {
    this.mapService.postRequest(this.secondFormGroup.get('address')?.value!)
    .subscribe((res: any) => {
      if (this.map?.marker != undefined) this.map?.marker.remove();
      this.map?.addMarkerAndAdjustView(res.latitude, res.longitude)

    }, (err: any) => {
      console.log(err)
    });
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
      }
      // if (this.img) {
      //   this.imgString = String(await this.toBase64(img));

      // };
    }
  }

  // toBase64 = (file: any) => new Promise((resolve, reject) => {
  //   const reader = new FileReader()
  //   reader.readAsDataURL(file)
  //   reader.onload = () => resolve(reader.result)
  //   reader.onerror = error => reject(error)
  // });

  deleteImage(): void {
    this.imgURL = null
    this.img = null
  }


}


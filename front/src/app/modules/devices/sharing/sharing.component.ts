import { Component, OnInit } from '@angular/core';
import { SharingService } from './sharing.service';
import { AuthService } from '../../auth/service/auth.service';
import { DeviceDTO, SharedDeviceDTO, SharedPropertyDTO } from 'src/app/model/model';
import { PropertyServiceService } from '../../property/service/property-service.service';
import { ImageServiceService } from '../../service/image-service.service';
import { Router } from '@angular/router';
import { OutdoorDeviceService } from '../outdoor/service/outdoor-device-service';
import { LargeEnergyService } from '../large-energy/large-energy.service';
import { IndoorDeviceService } from '../indoor/service/indoor-device.service';

@Component({
  selector: 'app-sharing',
  templateUrl: './sharing.component.html',
  styleUrls: ['./sharing.component.css']
})
export class SharingComponent implements OnInit {

  sharedProperties: SharedPropertyDTO[] = []
  hoveredPropertyId: string | null = null;
  isHovered = false;

  sharedDevices: SharedDeviceDTO[] = []

  constructor(private sharingService: SharingService, 
              private authService: AuthService, 
              private readonly propertyService: PropertyServiceService,
              public readonly imageService: ImageServiceService,
              private readonly outdoorDeviceService: OutdoorDeviceService,
              private readonly largeEnergyDeviceService: LargeEnergyService,
              private readonly indoorService: IndoorDeviceService,
              private router: Router) { }

  ngOnInit(): void {

    this.sharingService.getSharedWithProperties(this.authService.getId()).subscribe((properties: SharedPropertyDTO[]) => {
      this.sharedProperties = properties
      console.log(this.sharedProperties)
    })

    this.sharingService.getSharedWithDevices(this.authService.getId()).subscribe((devices: SharedDeviceDTO[]) => {
      this.sharedDevices = devices
      console.log(this.sharedDevices)
    })

  }

  showImage(propertyId: string): void {
    this.hoveredPropertyId = propertyId;
  }

  hideImage(): void {
    this.hoveredPropertyId = null;
  }


  handleImageError(event: any): void {
    // Handle the image error here, e.g., set a placeholder image
    this.isHovered = false
    event.target.src = '';
    event.target.hidden = true;
  }

  isPropertyHovered(propertyId: string): boolean {
    return this.hoveredPropertyId === propertyId;
  }

  navigateToSingleProperty() {
    const p = this.sharedProperties.find(sharedProperty => sharedProperty.property.id === this.hoveredPropertyId);
    if (p?.property?.propertyStatus != 'PENDING') {
      this.propertyService.setProperty(p?.property);
      this.router.navigate(['/property']);
    }
  }

  getDeviceTypeDisplayName(device: DeviceDTO): string {
    switch(device.deviceType) {
      case "lamp":
        return "Lamp"
      case "gate":
        return "Gate"
      case "sprinkler":
        return "Sprinkler"
      case "washingMachine":
        return "Washing machine"
      case "ambientSensor":
        return "Ambient sensor"
      case "airConditioner":
        return "Air conditioner"
      case "solarPanel":
        return "Solar panel"
      case "battery":
        return "Battery"
      default:
        return ""
    }
  }

  navigateToDevice(device: DeviceDTO) {
    let indoor: string[] = ["ambientSensor", "airConditioner", "washingMachine"]
    let outdoor: string[] = ["lamp", "gate", "sprinkler"]
    let large: string[] = ["solarPanel", "battery", "charger"]

    if (indoor.includes(device.deviceType))
      this.indoorService.setSelectedIndoorDeviceId(device.id)
    if (outdoor.includes(device.deviceType))
      this.outdoorDeviceService.setSelectedDeviceId(device.id)
    if (large.includes(device.deviceType))
      this.largeEnergyDeviceService.setSelectedDeviceId(device.id)

    if (device.deviceType === 'lamp') {
        this.router.navigate(['/lamp']);
    } else if (device.deviceType === "gate") {
        this.router.navigate(['/gate'])
    } else if (device.deviceType === "sprinkler") {
      this.router.navigate(['/sprinkler'])
    } else if (device.deviceType === 'solarPanel') {
      this.router.navigate(['/solarPanel']);
    } else if (device.deviceType == 'battery') {
      this.router.navigate(['/battery'])
    } else if (device.deviceType == 'charger') {
      this.router.navigate(['/charger'])
    } else if (device.deviceType === 'ambientSensor') {
      this.router.navigate(['/ambient-sensor']);
    } else if (device.deviceType == 'airConditioner') {
      this.router.navigate(['/air-conditioner'])
    } else if (device.deviceType == 'washingMachine') {
      this.router.navigate(['/washing-machine'])
    }
  }

}

import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { DeviceDTO, PropertyDTO } from 'src/app/model/model';
import { OutdoorDeviceService } from '../../devices/outdoor/service/outdoor-device-service';
import { PropertyServiceService } from '../service/property-service.service';
import { LargeEnergyService } from '../../devices/large-energy/large-energy.service';

@Component({
  selector: 'app-single-property',
  templateUrl: './single-property.component.html',
  styleUrls: ['./single-property.component.css']
})
export class SinglePropertyComponent implements OnInit {

  public property: PropertyDTO | undefined;

  appliances: DeviceDTO[] = []
  outdoor: DeviceDTO[] = []
  energyDevices: DeviceDTO[] = []

  constructor(private readonly propertyService: PropertyServiceService,
    private readonly outdoorDeviceService: OutdoorDeviceService,
    private readonly largeEnergyDeviceService: LargeEnergyService,
    private router: Router) { 
    this.propertyService.currentProperty.subscribe(
      (property: PropertyDTO | undefined) => (this.property = property)
    );

    this.propertyService.getApliences(this.property?.id || "0").subscribe((res: any) => {
      this.appliances = res;
    })

    this.propertyService.getOutdoor(this.property?.id || "0").subscribe((res: any) => {
      this.outdoor = res;
      console.log(res)

    })

    this.propertyService.getEnergyDevices(this.property?.id || "0").subscribe((res: any) => {
      this.energyDevices = res;
    })
  }


  ngOnInit(): void {
    
  }

  selectProperty(propertyId: string) {
    this.propertyService.setSelectedPropertyId(propertyId);
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

  navigateToOutdoorDevice(device: DeviceDTO) {
    this.outdoorDeviceService.setSelectedDeviceId(device.id);
    if (device.deviceType === 'lamp') {
        this.router.navigate(['/lamp']);
    } else if (device.deviceType === "gate") {
        this.router.navigate(['/gate'])
    } else if (device.deviceType === "sprinkler") {
      this.router.navigate(['/sprinkler'])
  }
  }

  navigateToLargeEnergyDevice(device: DeviceDTO) {
    this.largeEnergyDeviceService.setSelectedDeviceId(device.id);
    if (device.deviceType === 'solarPanel') {
        this.router.navigate(['/solarPanel']);
    }
    else if (device.deviceType == 'battery') {
      this.router.navigate(['/battery'])
    }
  }

  navigateToIndoorDevice(device: DeviceDTO) {
    if (device.deviceType === 'ambientSensor') {
      this.router.navigate(['/ambient-sensor']);
  }
  else if (device.deviceType == 'airConditioner') {
    this.router.navigate(['/air-conditioner'])
  }
  }

}

import { Component, OnInit } from '@angular/core';
import { LargeEnergyService } from '../large-energy.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-solar-panel',
  templateUrl: './solar-panel.component.html',
  styleUrls: ['./solar-panel.component.css']
})
export class SolarPanelComponent implements OnInit {

  panelId: string = ""
  constructor( private readonly largeEnergyDeviceService: LargeEnergyService,
    private router: Router) { 
      this.largeEnergyDeviceService.selectedDeviceId$.subscribe((res: string) => {
        this.panelId = res;
        console.log(this.panelId)
      })
 }

  ngOnInit(): void {
  }

}

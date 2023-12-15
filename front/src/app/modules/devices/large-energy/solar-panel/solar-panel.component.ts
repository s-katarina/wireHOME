import { Component, OnInit } from '@angular/core';
import { LargeEnergyService } from '../large-energy.service';
import { Router } from '@angular/router';
import { FormBuilder, FormControl, FormGroup } from '@angular/forms';
import { DatePipe } from '@angular/common';
import { CanvasJS } from '@canvasjs/angular-charts';

@Component({
  selector: 'app-solar-panel',
  templateUrl: './solar-panel.component.html',
  styleUrls: ['./solar-panel.component.css']
})
export class SolarPanelComponent implements OnInit {

  panelId: string = ""
  selectedOption = ""
  
  range = new FormGroup({
    start: new FormControl<Date | null>(null),
    end: new FormControl<Date | null>(null),
  });


  chart: any;
	
  constructor( private readonly largeEnergyDeviceService: LargeEnergyService,
    private router: Router,
    private fb: FormBuilder,
    private datePipe: DatePipe) { 
      this.largeEnergyDeviceService.selectedDeviceId$.subscribe((res: string) => {
        this.panelId = res;
        console.log(this.panelId)
      })


 }

  ngOnInit(): void {
    this.chart = new CanvasJS.Chart("chartContainer", 
    {
      zoomEnabled: true,
      exportEnabled: true,
      theme: "light2",
      title: {
      text: "Try Zooming & Panning"
      },
      data: [{
      type: "line",
      dataPoints: []
      }]
    })
    this.chart.render();
  }

  onSubmit() {
    const dateFrom = (this.range.value.start!.getTime()/1000).toString();
    const dateTo = (this.range.value.end!.getTime()/1000).toString();
    console.log('Date Range:', dateFrom, dateTo);
    //date range u milisekundama
    this.largeEnergyDeviceService.getSolarPlatformReadingFrom(this.panelId, dateFrom, dateTo).subscribe((res: any) => {
      this.chart.options.data[0].dataPoints = res;
      console.log(res)
      this.chart.render();

    })
  }

}

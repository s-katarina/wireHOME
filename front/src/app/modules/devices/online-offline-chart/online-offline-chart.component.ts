import { Component, OnInit, Input } from '@angular/core';
import { CanvasJS } from '@canvasjs/angular-charts';
import { LargeEnergyService } from '../large-energy/large-energy.service';

@Component({
  selector: 'app-online-offline-chart',
  templateUrl: './online-offline-chart.component.html',
  styleUrls: ['./online-offline-chart.component.css']
})
export class OnlineOfflineChartComponent implements OnInit {
  @Input() deviceId = '';
  pyChart: any

  constructor(private readonly largeEnergyDeviceService: LargeEnergyService,)
    { }

  ngOnInit(): void {
	CanvasJS.addColorSet("appColors",
	[
	"#556282",
	"#8da3b9",
	"#7f91bc92",
	"#acacbe"
	]);
    this.pyChart = new CanvasJS.Chart("chartOnlineContainer",
	{
		colorSet: "appColors",

		title:{
			text: "How much has your device been online in last 24 hours",
			fontFamily: 'Sora-semibold'
		},
		
		legend: {
			maxWidth: 350,
			itemWidth: 120
		},
		toolTip: {
			enabled: false 
		},
		data: [
		{
			type: "pie",
			showInLegend: true,
			legendText: "{indexLabel}",
			dataPoints: []
		}
		]
	});
  
  this.largeEnergyDeviceService.getDeviceOnlineOfflinePyChart(this.deviceId).subscribe((res: any) => {
    console.log(res);
    this.pyChart.options.data[0].dataPoints = res
    this.pyChart.render();
  });
  }

}

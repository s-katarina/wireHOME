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
    this.pyChart = new CanvasJS.Chart("PYCHARTchartContainer",
	{
		title:{
			text: "How much has your device been online"
		},
		legend: {
			maxWidth: 350,
			itemWidth: 120
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
  console.log("alobre" + this.deviceId)
  
  this.largeEnergyDeviceService.getDeviceOnlineOfflinePyChart(this.deviceId).subscribe((res: any) => {
    console.log(res);
    this.pyChart.options.data[0].dataPoints = res
    this.pyChart.render();
  });
  }

}

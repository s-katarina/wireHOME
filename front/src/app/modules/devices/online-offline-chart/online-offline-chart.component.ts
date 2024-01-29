import { Component, OnInit, Input } from '@angular/core';
import { CanvasJS } from '@canvasjs/angular-charts';
import { LargeEnergyService } from '../large-energy/large-energy.service';
import { FormGroup, FormControl } from '@angular/forms';

@Component({
  selector: 'app-online-offline-chart',
  templateUrl: './online-offline-chart.component.html',
  styleUrls: ['./online-offline-chart.component.css']
})
export class OnlineOfflineChartComponent implements OnInit {
  @Input() deviceId = '';
  pyChart: any

  dateFrom = ""
  dateTo = ""
  rangeOver30Days = false

  selectedOption: string = "24h"

  range = new FormGroup({
    start: new FormControl<Date | null>(null),
    end: new FormControl<Date | null>(null),
  });

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
    this.pyChart = new CanvasJS.Chart("PYCHARTchartContainer",
	{
		colorSet: "appColors",
		
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
  
	// Get data for last 24 hours on init
	let dayBeforeTimestamp = (new Date()).setDate((new Date()).getDate() - 1	)
	this.largeEnergyDeviceService.getDeviceOnlineOfflinePyChart(this.deviceId, (Math.floor(dayBeforeTimestamp)).toString(), (Math.floor((new Date()).getTime())).toString()).subscribe((res: any) => {
		console.log(res);
		this.pyChart.options.data[0].dataPoints = res
		this.pyChart.render();
	});
  }

  onSubmit() {

	if (this.dateFrom === "" || this.dateTo === "") return
    if (this.selectedOption == "range") {
      if ((this.range.value.start != null && this.range.value.start != null) 
        && this.range.controls.start.valid && this.range.controls.end.valid) {
          this.dateFrom = (this.range.value.start!.getTime()).toString();
          this.dateTo = (this.range.value.end!.getTime()).toString();

          const dateFromTimestamp = parseInt(this.dateFrom, 10);
          const dateToTimestamp = parseInt(this.dateTo, 10);
          const timeDifference = Math.abs(dateToTimestamp - dateFromTimestamp);
          const daysDifference = timeDifference / (1000 * 60 * 60 * 24);
          if (daysDifference > 30) {
            console.log("Date range is more than 30 days apart");
            this.rangeOver30Days = true
            return;
          } else {
            console.log("Date range is within 30 days");
            this.rangeOver30Days = false
          }

        }
    }
	  
	this.largeEnergyDeviceService.getDeviceOnlineOfflinePyChart(this.deviceId, this.dateFrom, this.dateTo).subscribe((res: any) => {
		console.log(res);
		this.pyChart.options.data[0].dataPoints = res
		this.pyChart.render();
	});
  
  }

  // Updates dateFrom and dateTo
  onDropdownChange() {
    let dateBefore = new Date().getTime()
    const currentDate = new Date();
    console.log('Selected Value:', this.selectedOption);
    if (this.selectedOption.includes("d")) {
      const stringWithoutD = this.selectedOption.replace('d', '');
      const resultNumber = Number(stringWithoutD);
      dateBefore = (new Date()).setDate(currentDate.getDate() - resultNumber);
    } else if (this.selectedOption.includes("h")) {
      const stringWithoutD = this.selectedOption.replace('h', '');
      const resultNumber = Number(stringWithoutD);
      const currentDate = new Date();
      dateBefore = (new Date()).setHours(currentDate.getHours() - resultNumber);
    }
    this.dateFrom = (Math.floor(dateBefore)).toString();
    this.dateTo = (Math.floor(currentDate.getTime())).toString();
    console.log('Date Range:', this.dateFrom, this.dateTo);
  }

}

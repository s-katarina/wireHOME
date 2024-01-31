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
  intervalChart: any
  timeUnitChart: any

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

  var yLabels = ["Offline","Online"];

  this.intervalChart = new CanvasJS.Chart("intervalChartContainer", 
    {
      colorSet: "appColors",
      zoomEnabled: true,
      theme: "light2",
      axisY: {
        interval: 1,
        labelFormatter: function (e: any) {  
          if (e.value != 1 && e.value != 0) return "" 
          return yLabels[e.value];
        },
        maximum: 1.1
      },
      data: [{
      type: "line",
      xValueType: "dateTime",
      dataPoints: []
      }]
    })
  this.intervalChart.render();
  
  this.timeUnitChart = new CanvasJS.Chart("timeUnitChartContainer", 
    {
      colorSet: "appColors",
      theme: "light2",
      axisX: {
        intervalType: "dateTime",
      },
      axisY: {
        suffix: "%"
      },
      toolTip: {
        shared: true
      },
      legend: {
        reversed: true,
        verticalAlign: "center",
        horizontalAlign: "right"
      },
      dataPointWidth: 20,
      data: [{
        type: "stackedColumn100",
        xValueType: "dateTime",
        name: "Online",
        showInLegend: true,
        yValueFormatString: "#,##0\"%\"",
        dataPoints: []
      },
      {
        type: "stackedColumn100",
        xValueType: "dateTime",
        name: "Offline",
        showInLegend: true,
        yValueFormatString: "#,##0\"%\"",
        dataPoints: []
      }]
    })
  this.timeUnitChart.render();

	// Get data for last 24 hours on init
	let dayBeforeTimestamp = (new Date()).setDate((new Date()).getDate() - 1	)
	this.largeEnergyDeviceService.getDeviceOnlineOfflinePyChart(this.deviceId, (Math.floor(dayBeforeTimestamp)).toString(), (Math.floor((new Date()).getTime())).toString()).subscribe((res: any) => {
		this.pyChart.options.data[0].dataPoints = res
		this.pyChart.render();
	});

  this.largeEnergyDeviceService.getDeviceOnlineOfflineIntervalChart(this.deviceId, (Math.floor(dayBeforeTimestamp)).toString(), (Math.floor((new Date()).getTime())).toString()).subscribe((res: any) => {
		if (res.status == 200) {
      const dataPoints = res.data.map((item: { timestamp: string; value: string; }) => ({
        x: parseInt(item.timestamp),
        y: Math.floor(parseFloat(item.value))
      }));
      this.intervalChart.options.data[0].dataPoints = dataPoints;
      this.intervalChart.render();
    }
	});

  this.largeEnergyDeviceService.getDeviceOnlineOfflineTimeUnitChart(this.deviceId, (Math.floor(dayBeforeTimestamp)).toString(), (Math.floor((new Date()).getTime())).toString()).subscribe((res: any) => {
		if (res.status == 200) {
      console.log(res.data)
      const dataPoints = res.data.map((item: { timestamp: string; value: string; }) => ({
        x: parseInt(item.timestamp),
        y: parseFloat(item.value)  * 100
      }));
      console.log(dataPoints)
      const offlineDataPoints = res.data.map((item: { timestamp: string; value: string; }) => ({
        x: parseInt(item.timestamp),
        y: 100 - (parseFloat(item.value) * 100)
      }));
      this.timeUnitChart.options.data[0].dataPoints = dataPoints;
      this.timeUnitChart.options.data[1].dataPoints = offlineDataPoints;
      this.timeUnitChart.render();
    }
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
		this.pyChart.options.data[0].dataPoints = res
		this.pyChart.render();
	});

  this.largeEnergyDeviceService.getDeviceOnlineOfflineIntervalChart(this.deviceId, this.dateFrom, this.dateTo).subscribe((res: any) => {
		if (res.status == 200) {
      const dataPoints = res.data.map((item: { timestamp: string; value: string; }) => ({
        x: parseInt(item.timestamp),
        y: Math.floor(parseFloat(item.value))
      }));
      this.intervalChart.options.data[0].dataPoints = dataPoints;
      this.intervalChart.render();
    }
	});

  this.largeEnergyDeviceService.getDeviceOnlineOfflineTimeUnitChart(this.deviceId, this.dateFrom, this.dateTo).subscribe((res: any) => {
		if (res.status == 200) {
      console.log(res.data)
      const dataPoints = res.data.map((item: { timestamp: string; value: string; }) => ({
        x: parseInt(item.timestamp),
        y: parseFloat(item.value) * 100
      }));
      const offlineDataPoints = res.data.map((item: { timestamp: string; value: string; }) => ({
        x: parseInt(item.timestamp),
        y: 100 - (parseFloat(item.value) * 100)
      }));
      console.log(dataPoints)
      this.timeUnitChart.options.data[0].dataPoints = dataPoints;
      this.timeUnitChart.options.data[1].dataPoints = offlineDataPoints;
      this.timeUnitChart.render();
    }
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

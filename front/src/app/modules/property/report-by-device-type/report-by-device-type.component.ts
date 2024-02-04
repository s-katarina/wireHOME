import { Component, Input, OnInit } from '@angular/core';
import { PropertyServiceService } from '../service/property-service.service';
import { StartEnd } from 'src/app/model/model';
import { CanvasJS } from '@canvasjs/angular-charts';
import { FormControl, FormGroup } from '@angular/forms';

@Component({
  selector: 'app-report-by-device-type',
  templateUrl: './report-by-device-type.component.html',
  styleUrls: ['./report-by-device-type.component.css']
})
export class ReportByDeviceTypeComponent implements OnInit {

  @Input() deviceId = "";
  pyChart: any

  rangeOVaj = new FormGroup({
    start: new FormControl<Date | null>(null),
    end: new FormControl<Date | null>(null),
  });

  constructor(private readonly propertyService: PropertyServiceService) { }

  ngOnInit(): void {
    let datee: StartEnd = this.getDate()
    console.log(datee)
    this.fillChart(datee);
  }

  private fillChart(datee: StartEnd) {
    CanvasJS.addColorSet("appColors",
      [
        "#556282",
        "#8da3b9",
        "#7f91bc92",
        "#acacbe"
      ]);
    this.pyChart = new CanvasJS.Chart("PYCHARTchartLAST",
      {
        colorSet: "appColors",

        title: {
          text: "Consumation By Device Type",
          fontSize: 25,
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
    // this.pyChart.render();
    this.propertyService.getByDeviceTypeForProperty(datee.start, datee.end, this.deviceId).subscribe((res: any) => {
      console.log(res);
      this.pyChart.options.data[0].dataPoints = res;
      this.pyChart.render();
    });
  }

  changeChartOVaj() {
    if ((this.rangeOVaj.value.start != null && this.rangeOVaj.value.start != null) 
    && this.rangeOVaj.controls.start.valid && this.rangeOVaj.controls.end.valid) { 
      let start = Math.floor(this.rangeOVaj.value.start!.getTime())/1000
      let end = Math.floor(this.rangeOVaj.value.end!.getTime())/1000
      this.propertyService.getByDeviceTypeForProperty(start, end, this.deviceId).subscribe((res: any) => {
        console.log(res);
        this.pyChart.options.data[0].dataPoints = res;
        this.pyChart.render();
      });
    }
    }

  public getDate(): StartEnd {
    let dateBefore = new Date().getTime()
    let currentDate = new Date();
    dateBefore = (new Date()).setDate(currentDate.getDate() - 7);
    const dateFrom = (Math.floor(dateBefore/1000));
    const dateTo = (Math.floor(currentDate.getTime()/1000));
    return {
      start: dateFrom,
      end: dateTo
    }
  }
}

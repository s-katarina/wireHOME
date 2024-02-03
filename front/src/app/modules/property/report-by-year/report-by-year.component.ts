import { Component, Input, OnInit } from '@angular/core';
import { CanvasJS } from '@canvasjs/angular-charts';
import { PropertyServiceService } from '../service/property-service.service';

@Component({
  selector: 'app-report-by-year',
  templateUrl: './report-by-year.component.html',
  styleUrls: ['./report-by-year.component.css']
})
export class ReportByYearComponent implements OnInit {
  @Input() id: string = "";
  @Input() whatId = 'property-id'
  year: number = 2024;
  barChartElec: any
  constructor(private readonly propertyService: PropertyServiceService) { }

  ngOnInit(): void {
    CanvasJS.addColorSet("appColors",
	[
	"#556282",
	"#8da3b9",
	// "#7f91bc92",
	// "#acacbe"
	]);

    this.barChartElec = new CanvasJS.Chart("barChartt", 
    {
      exportEnabled: true,

      animationEnabled: true,
      colorSet: "appColors",

		// title:{
		// 	text: "Consumption By Month",
		// 	fontFamily: 'Sora-semibold'
		// },
      
      toolTip: {
      shared: true
      },
      legend:{
      cursor:"pointer",
      itemclick: function(e: any){
        if (typeof(e.dataSeries.visible) === "undefined" || e.dataSeries.visible) {
        e.dataSeries.visible = false;
        }
        else {
        e.dataSeries.visible = true;
        }
        e.barChartElec.render();
      }
      },
      data: [{
      type: "column",	
      name: "Electricity spent (kW)",
      legendText: "Property consumption",
      showInLegend: true, 
      dataPoints:[]
      },
      {
        type: "column",	
        name: "Electricity from electrodistribution (kW)",
        legendText: "Electrodistribution",
        axisYType: "secondary",
        showInLegend: true, 
        dataPoints:[]
        }]
    })
    
    this.barChartElec.render();
    this.renderBars();
  }

  private renderBars() {
    this.propertyService.getByMonthProperty(this.id || '', this.year, "property-electricity", this.whatId).subscribe((res: any) => {
      console.log(res);
      this.barChartElec.options.data[0].dataPoints = res;
      this.barChartElec.render();
    });

    this.propertyService.getByMonthProperty(this.id || '', this.year, "electrodeposition", this.whatId).subscribe((res: any) => {
      console.log(res);
      this.barChartElec.options.data[1].dataPoints = res;
      this.barChartElec.render();
    });
  }

  changeYear() {
    if (this.year>2016 && this.year<2025) {
        this.renderBars()
    }
  }

}

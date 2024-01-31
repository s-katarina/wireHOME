import { Component, OnInit } from '@angular/core';
import { PropertyServiceService } from '../service/property-service.service';
import { ByTimeOfDay, LabeledGraphDTO, PropertyDTO, StartEnd } from 'src/app/model/model';
import { CanvasJS } from '@canvasjs/angular-charts';
import { FormControl, FormGroup } from '@angular/forms';

@Component({
  selector: 'app-propert-consumption-overview',
  templateUrl: './propert-consumption-overview.component.html',
  styleUrls: ['./propert-consumption-overview.component.css']
})
export class PropertConsumptionOverviewComponent implements OnInit {


  public property: PropertyDTO | undefined;

  barChartElec: any;

  labledGraphData: LabeledGraphDTO[] = []
  dayCharts: any[] = []

  timeOfDay: ByTimeOfDay | undefined

  year: number = 2024;
  rangeProperty = new FormGroup({
    start: new FormControl<Date | null>(null),
    end: new FormControl<Date | null>(null),
  });
elecTotal: number = 0;
distTotal: number = 0;


  constructor(private readonly propertyService: PropertyServiceService) { }

  ngOnInit(): void {
    this.propertyService.currentProperty.subscribe(
      (property: PropertyDTO | undefined) => (this.property = property)
    );
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

		title:{
			text: "Consumption By Month",
			fontFamily: 'Sora-semibold'
		},
      
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

    let datee: StartEnd = this.getDate()
    this.propertyService.getTimeOfDay(this.property?.id, datee.start, datee.end).subscribe((res: any) => {
      console.log(res)
      this.timeOfDay = res
      this.elecTotal = (this.timeOfDay?.dayElec || 0 )+ (this.timeOfDay?.nightElec || 0)
      this.distTotal = (this.timeOfDay?.dayDist || 0 )+ (this.timeOfDay?.nightDist || 0)

    })
    this.fillDayGrafs(datee)

  }

  public fillDayGrafs(datee: StartEnd) { 
    this.propertyService.getPropertyByDayReadingFrom(this.property?.id || "0", datee.start, datee.end, "property-electricity").subscribe((res: any) => {
      console.log(res)
      this.labledGraphData = res
      for (const  labeledData of this.labledGraphData) {
        let chart = new CanvasJS.Chart(labeledData.label, 
        {
          zoomEnabled: true,
          exportEnabled: true,
          theme: "light2",
          title: {
          text: labeledData.label
          },
          data: [{
          type: "line",
          xValueType: "dateTime",
          dataPoints: [labeledData.graphDTOS]
          }]
        })
        chart.render();
      }

    })

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

  private renderBars() {
    this.propertyService.getByMonthProperty(this.property?.id || '', this.year, "property-electricity").subscribe((res: any) => {
      console.log(res);
      this.barChartElec.options.data[0].dataPoints = res;
      this.barChartElec.render();
    });

    this.propertyService.getByMonthProperty(this.property?.id || '', this.year, "electrodeposition").subscribe((res: any) => {
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

  changeByPartOfDay(){
    if ((this.rangeProperty.value.start != null && this.rangeProperty.value.start != null) 
    && this.rangeProperty.controls.start.valid && this.rangeProperty.controls.end.valid) { 
      let start = Math.floor(this.rangeProperty.value.start!.getTime())/1000
      let end = Math.floor(this.rangeProperty.value.end!.getTime())/1000
      this.propertyService.getTimeOfDay(this.property?.id, start, end).subscribe((res: any) => {
        console.log(res)
        this.timeOfDay = res
        this.elecTotal = (this.timeOfDay?.dayElec || 0 )+ (this.timeOfDay?.nightElec || 0)
        this.distTotal = (this.timeOfDay?.dayDist || 0 )+ (this.timeOfDay?.nightDist || 0)
      })
    }
  }

}

import { Component, OnInit } from '@angular/core';
import { PropertyServiceService } from '../service/property-service.service';
import { ByTimeOfDay, LabeledGraphDTO, PropertyDTO, StartEnd } from 'src/app/model/model';
import { CanvasJS } from '@canvasjs/angular-charts';
import { FormControl, FormGroup } from '@angular/forms';
import { LargeEnergyService } from '../../devices/large-energy/large-energy.service';

@Component({
  selector: 'app-propert-consumption-overview',
  templateUrl: './propert-consumption-overview.component.html',
  styleUrls: ['./propert-consumption-overview.component.css']
})
export class PropertConsumptionOverviewComponent implements OnInit {


  public property: PropertyDTO | undefined;
  selectedOption: string = ""
  selectedOptionElectro: string = ''
  
  range = new FormGroup({
    start: new FormControl<Date | null>(null),
    end: new FormControl<Date | null>(null),
  });
  chart: any;
  socketChart:any
  electrodistChart:any

  rangeOver30Days = false
  
  constructor(private readonly propertyService: PropertyServiceService,
    private readonly largeEnergyDeviceService: LargeEnergyService) { }

  ngOnInit(): void {
    this.propertyService.currentProperty.subscribe(
      (property: PropertyDTO | undefined) => (this.property = property)
    );
    this.chart = new CanvasJS.Chart("chartContainer", 
    {
      zoomEnabled: true,
      exportEnabled: true,
      theme: "light2",
      title: {
      text: "Property consumption"
      },
      data: [{
      type: "line",
      xValueType: "dateTime",
      dataPoints: []
      }]
    })
    this.chart.render();


    this.electrodistChart = new CanvasJS.Chart("energyChartContainer", 
    {
      zoomEnabled: true,
      exportEnabled: true,
      theme: "light2",
      title: {
      text: "Electrodistribution transactions"
      },
      data: [{
      type: "line",
      xValueType: "dateTime",
      dataPoints: []
      }]
    })
    this.electrodistChart.render();
    
  }

  onSubmit(topic: string) {

    let dateFromX = (this.range.value.start!.getTime()).toString();
    let dateToX  = (this.range.value.end!.getTime()).toString();

    const dateFromTimestamp = parseInt(dateFromX, 10);
    const dateToTimestamp = parseInt(dateToX, 10);
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

    const dateFrom = (this.range.value.start!.getTime()/1000).toString();
    const dateTo = (this.range.value.end!.getTime()/1000).toString();
    console.log('Date Range:', dateFrom, dateTo);
    //date range u milisekundama
    this.largeEnergyDeviceService.getPropertyReadingFrom(this.property?.id || "", dateFrom, dateTo, topic).subscribe((res: any) => {
      this.whatChartToRender(res, topic);

    })
  }

  private whatChartToRender(res: any, topic:string) {
    if (topic === "property-electricity") {
      this.chart.options.data[0].dataPoints = res;
      console.log(res);
      this.chart.render();
    } else if (topic === "electrodeposition"){
      this.electrodistChart.options.data[0].dataPoints = res;
      console.log(res);
      this.electrodistChart.render();
    }
    
  }

  onDropdownChange(event: any, topic: string) {
    // Handle the change event here
    // const selectedValue: string = event.target.value;
    // console.log('Selected Value:', this.selectedOption);
    var currentOption = ''
    currentOption = this.selectedOptionElectro
    if (topic === "property-electricity") {
      currentOption = this.selectedOption
    }
    if (currentOption == "range") return
    console.log(currentOption)
    console.log(topic)
    // this.selectedOption = selectedValue
    // Add your custom logic based on the selected value
    var dateBefore = new Date().getTime()
    var currentDate = new Date();
    if (currentOption.includes("d")) {
      const stringWithoutD = currentOption.replace('d', '');
      const resultNumber = Number(stringWithoutD);
      const currentDate = new Date();

      // Subtract 3 hours
      dateBefore = (new Date()).setDate(currentDate.getDate() - resultNumber);
      console.log("Current date:", currentDate);
      console.log("Date 7 days before:", dateBefore);

    }
    else if (currentOption.includes("h")) {
      const stringWithoutD = currentOption.replace('h', '');
      const resultNumber = Number(stringWithoutD);
      const currentDate = new Date();

      // Subtract 3 hours
      dateBefore = (new Date()).setHours(currentDate.getHours() - resultNumber);
    }

    const dateFrom = (Math.floor(dateBefore/1000)).toString();
    const dateTo = (Math.floor(currentDate.getTime()/1000)).toString();
    console.log('Date Range:', dateFrom, dateTo);
    //date range u milisekundama
    this.largeEnergyDeviceService.getPropertyReadingFrom(this.property?.id || "", dateFrom, dateTo, topic).subscribe((res: any) => {
      this.whatChartToRender(res, topic);

    })
  }

}

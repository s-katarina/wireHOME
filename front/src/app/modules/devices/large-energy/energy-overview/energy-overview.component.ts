import { Component, OnInit } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { CanvasJS } from '@canvasjs/angular-charts';
import { PropertyServiceService } from 'src/app/modules/property/service/property-service.service';
import { LargeEnergyService } from '../large-energy.service';

@Component({
  selector: 'app-energy-overview',
  templateUrl: './energy-overview.component.html',
  styleUrls: ['./energy-overview.component.css']
})
export class EnergyOverviewComponent implements OnInit {
  propertyId: string = '';
  selectedOption: string = ""
  selectedOptionElectro: string = ''
  
  range = new FormGroup({
    start: new FormControl<Date | null>(null),
    end: new FormControl<Date | null>(null),
  });
  chart: any;
  socketChart:any
  electrodistChart:any

  constructor(private readonly propertyService: PropertyServiceService,
    private readonly largeEnergyDeviceService: LargeEnergyService,) { }

  ngOnInit(): void {
    this.propertyService.selectedPropertyId$.subscribe((propertyId) => {
      this.propertyId = propertyId;
      console.log(this.propertyId)
    });
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

    this.socketChart = new CanvasJS.Chart("socketChartContainer", 
    {
      zoomEnabled: true,
      exportEnabled: true,
      theme: "light2",
      title: {
      text: "Realtime property consumption"
      },
      data: [{
      type: "line",
      xValueType: "dateTime",
      dataPoints: []
      }]
    })
    this.getSocketData()

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
    this.chart.render();
    
  }


  onSubmit(topic: string) {
    const dateFrom = (this.range.value.start!.getTime()/1000).toString();
    const dateTo = (this.range.value.end!.getTime()/1000).toString();
    console.log('Date Range:', dateFrom, dateTo);
    //date range u milisekundama
    this.largeEnergyDeviceService.getPropertyReadingFrom(this.propertyId, dateFrom, dateTo, topic).subscribe((res: any) => {
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
    this.largeEnergyDeviceService.getPropertyReadingFrom(this.propertyId, dateFrom, dateTo, topic).subscribe((res: any) => {
      this.whatChartToRender(res, topic);

    })
  }

  getSocketData() {
    this.socketChart.render();
    return []
  }

}



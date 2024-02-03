import { Component, Input, OnInit } from '@angular/core';
import { PropertyServiceService } from '../service/property-service.service';
import { LabeledGraphDTO, StartEnd } from 'src/app/model/model';
import { CanvasJS } from '@canvasjs/angular-charts';

@Component({
  selector: 'app-report-by-week',
  templateUrl: './report-by-week.component.html',
  styleUrls: ['./report-by-week.component.css']
})
export class ReportByWeekComponent implements OnInit {

  @Input() id = "";
  labledGraphData: LabeledGraphDTO[] = []
  dayCharts: any[] = []


  constructor(private readonly propertyService: PropertyServiceService) { }

  ngOnInit(): void {
    let datee: StartEnd = this.getDate()
    
    this.fillDayGrafs(datee)
    this.fillDayGrafsElec(datee)
  }
  public fillDayGrafs(datee: StartEnd) { 
    this.propertyService.getPropertyByDayReadingFrom(this.id || "0", datee.start, datee.end, "property-electricity").subscribe((res: any) => {
      console.log(res)
      this.labledGraphData = res
      let i = 0
      for (const  labeledData of this.labledGraphData) {
        i = i +1
        const chart = new CanvasJS.Chart(labeledData.label, 
        {
          zoomEnabled: true,
          theme: "light2",
          title: {
          text: labeledData.label
          },
          data: [{
          type: "line",
          xValueType: "dateTime",
          dataPoints: labeledData.graphDTOS
          }]
        })
        // chart.render();
        this.dayCharts.push(chart)
      }

      for (const  chaart of this.dayCharts){
        chaart.render()
      }

    })

  }

  public fillDayGrafsElec(datee: StartEnd) { 
    this.propertyService.getPropertyByDayReadingFrom(this.id || "0", datee.start, datee.end, "electrodeposition").subscribe((res: any) => {
      console.log(res)
      this.labledGraphData = res
      let i = 0
      for (const  labeledData of this.labledGraphData) {
        i = i +1
        const chart = new CanvasJS.Chart(labeledData.label + "2", 
        {
          zoomEnabled: true,
          theme: "light2",
          title: {
          text: labeledData.label
          },
          data: [{
          type: "line",
          xValueType: "dateTime",
          dataPoints: labeledData.graphDTOS
          }]
        })
        chart.render();
        // this.dayCharts.push(chart)
      }

      // for (const  chaart of this.dayCharts){
      //   chaart.render()
      // }

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
}

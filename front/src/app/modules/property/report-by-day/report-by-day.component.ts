import { Component, Input, OnInit } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { ByTimeOfDay, StartEnd } from 'src/app/model/model';
import { PropertyServiceService } from '../service/property-service.service';

@Component({
  selector: 'app-report-by-day',
  templateUrl: './report-by-day.component.html',
  styleUrls: ['./report-by-day.component.css']
})
export class ReportByDayComponent implements OnInit {

  @Input() id = ""
  @Input() whatId = 'property-id'


  timeOfDay: ByTimeOfDay | undefined

  rangeProperty = new FormGroup({
    start: new FormControl<Date | null>(null),
    end: new FormControl<Date | null>(null),
  });
elecTotal: number = 0;
distTotal: number = 0;

  constructor(private readonly propertyService: PropertyServiceService) { }

  ngOnInit(): void {
    let datee: StartEnd = this.getDate()
    this.propertyService.getTimeOfDay(this.id, datee.start, datee.end, this.whatId).subscribe((res: any) => {
      console.log(res)
      this.timeOfDay = res
      this.elecTotal = (this.timeOfDay?.dayElec || 0 )+ (this.timeOfDay?.nightElec || 0)
      this.distTotal = (this.timeOfDay?.dayDist || 0 )+ (this.timeOfDay?.nightDist || 0)

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

  changeByPartOfDay(){
    if ((this.rangeProperty.value.start != null && this.rangeProperty.value.start != null) 
    && this.rangeProperty.controls.start.valid && this.rangeProperty.controls.end.valid) { 
      let start = Math.floor(this.rangeProperty.value.start!.getTime())/1000
      let end = Math.floor(this.rangeProperty.value.end!.getTime())/1000
      this.propertyService.getTimeOfDay(this.id, start, end, this.whatId).subscribe((res: any) => {
        console.log(res)
        this.timeOfDay = res
        this.elecTotal = (this.timeOfDay?.dayElec || 0 )+ (this.timeOfDay?.nightElec || 0)
        this.distTotal = (this.timeOfDay?.dayDist || 0 )+ (this.timeOfDay?.nightDist || 0)
      })
    }
  }

}

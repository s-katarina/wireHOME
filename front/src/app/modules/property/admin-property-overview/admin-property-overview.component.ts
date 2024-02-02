import { Component, OnInit, ViewChild } from '@angular/core';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { MatTable, MatTableDataSource } from '@angular/material/table';
import { CityOverview, PropertyDTO, StartEnd } from 'src/app/model/model';
import { PropertyServiceService } from '../service/property-service.service';
import { Router } from '@angular/router';
import { FormControl, FormGroup } from '@angular/forms';
import { CanvasJS } from '@canvasjs/angular-charts';

@Component({
  selector: 'app-admin-property-overview',
  templateUrl: './admin-property-overview.component.html',
  styleUrls: ['./admin-property-overview.component.css']
})
export class AdminPropertyOverviewComponent implements OnInit {


  public allPropertyes: PropertyDTO[] = []
  public propertyes: PropertyDTO[] = []
  displayedColumnsProperty : string[] = ['type', 'country', 'city', 'ownerEmail', 'ownerName', 'energy', 'electodistribution', 'info']
  dataSourceProperty!: MatTableDataSource<PropertyDTO>;
  public cityes: CityOverview[] = []
  public allCityes: CityOverview[] = []
  displayedColumnsCyty : string[] = ['city', 'propertyNum', 'country', 'energy', 'electodistribution', 'info']
  dataSourceCity!: MatTableDataSource<CityOverview>;

  @ViewChild('paginatorFirst') paginator!: MatPaginator;
  @ViewChild('empTbSort') sort = new MatSort();
  @ViewChild(MatTable) eventTable!: MatTable<any>;

  @ViewChild('paginatorSecond') paginatorCity!: MatPaginator;
  @ViewChild('empTbSortWithObject') sortCity = new MatSort();
  @ViewChild(MatTable) eventTableCity!: MatTable<any>;

  pyChart: any

  public currentPageProperty : number = 0;
  public pageSizeProperty : number = 10;
  public lengthProperty : number = 0;

  public currentPageCity : number = 0;
  public pageSizeCity : number = 10;
  public lengthCity : number = 0;
  filterPropType: string = '';
  filterCountry: string = ''
  filterCity: string = ''
  filterEmail: string = ''
  filterOwnerName: string = ''
  filterAppliedProperty: boolean = false;
filterCountry2: string = ''
filterCity2: string = ''
  filterAppliedCity: boolean = false;
  rangeProperty = new FormGroup({
    start: new FormControl<Date | null>(null),
    end: new FormControl<Date | null>(null),
  });

  rangeCity = new FormGroup({
    start: new FormControl<Date | null>(null),
    end: new FormControl<Date | null>(null),
  });

  constructor(private readonly propertyService: PropertyServiceService,
              private readonly router: Router) { }

  ngOnInit(): void {
    let datee: StartEnd = this.getDate()
    console.log(datee)
    this.fillChart(datee);

    this.propertyTable(datee);

    this.cityTable(datee);
  }


  private fillChart(datee: StartEnd) {
    CanvasJS.addColorSet("appColors",
      [
        "#556282",
        "#8da3b9",
        "#7f91bc92",
        "#acacbe"
      ]);
    this.pyChart = new CanvasJS.Chart("PYCHARTchartContainer22",
      {
        colorSet: "appColors",

        title: {
          // text: "Distribution by city",
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
    this.propertyService.getByCityChart(datee.start, datee.end).subscribe((res: any) => {
      console.log(res);
      this.pyChart.options.data[0].dataPoints = res;
      this.pyChart.render();
    });
  }

  private cityTable(datee: StartEnd) {
    this.dataSourceCity = new MatTableDataSource<CityOverview>(this.cityes);
    this.dataSourceCity.paginator = this.paginatorCity;
    this.propertyService.getCityForOverview(datee.start, datee.end).subscribe((res: any) => {
      console.log(res);
      this.allCityes = res;
      this.cityes = res;
      this.dataSourceCity = new MatTableDataSource<CityOverview>(this.cityes);
      this.eventTableCity.renderRows();
      this.dataSourceCity.sort = this.sortCity;
      this.dataSourceCity.paginator = this.paginatorCity;
      this.lengthCity = this.cityes.length;
    });
  }

  private propertyTable(datee: StartEnd) {
    this.dataSourceProperty = new MatTableDataSource<PropertyDTO>(this.propertyes);
    this.dataSourceProperty.paginator = this.paginator;
    this.propertyService.getAcceptedProperties(datee.start, datee.end).subscribe((res: any) => {
      console.log(res);
      this.propertyes = res;
      this.allPropertyes = res;
      this.dataSourceProperty = new MatTableDataSource<PropertyDTO>(this.propertyes);
      this.eventTable.renderRows();
      this.dataSourceProperty.sort = this.sort;
      this.dataSourceProperty.paginator = this.paginator;
      this.lengthProperty = this.propertyes.length;
    });
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

  public handlePage(event?:any) {
    this.currentPageProperty = event.pageIndex;
    this.pageSizeProperty = event.pageSize;
    this.pageIteration();
    this.dataSourceProperty.sort = this.sort;
  }
  public handlePage2(event?:any) {
    this.currentPageCity = event.pageIndex;
    this.pageSizeCity = event.pageSize;
    this.pageIteration2();
    this.dataSourceCity.sort = this.sortCity;
  }
  
  private pageIteration() {
    const end = (this.currentPageProperty + 1) * this.pageSizeProperty;
    const start = this.currentPageProperty * this.pageSizeProperty;
    const part = this.propertyes.slice(start, end);
    this.dataSourceProperty = new MatTableDataSource<PropertyDTO>(part);

    this.dataSourceProperty.data = part;
    this.lengthProperty = this.propertyes.length;
  }
  private pageIteration2() {
    const end = (this.currentPageCity + 1) * this.pageSizeCity;
    const start = this.currentPageCity * this.pageSizeCity;
    const part = this.cityes.slice(start, end);
    this.dataSourceCity = new MatTableDataSource<CityOverview>(part);

    this.dataSourceCity.data = part;
    this.lengthCity = this.cityes.length;
  }

  clearFilterProperty() {
    this.filterAppliedProperty = false;
    this.filterPropType = '';
    this.filterCountry =''
    this.filterCity = ''
    this.filterEmail = ''
    this.filterOwnerName = ''
    this.propertyes = this.allPropertyes
    this.dataSourceProperty = new MatTableDataSource<PropertyDTO>(this.propertyes);
    this.dataSourceProperty.sort = this.sort;
    this.dataSourceProperty.paginator = this.paginator;
    this.lengthProperty = this.propertyes.length;
    }


  applyFilterProperty() {
    this.filterAppliedProperty = true;
    if ((this.rangeProperty.value.start != null && this.rangeProperty.value.start != null) 
    && this.rangeProperty.controls.start.valid && this.rangeProperty.controls.end.valid) { 
      let start = Math.floor(this.rangeProperty.value.start!.getTime())/1000
      let end = Math.floor(this.rangeProperty.value.end!.getTime())/1000
      this.propertyService.getAcceptedProperties(start, end).subscribe((res: any) => {
        this.propertyes = res
        console.log('prop' + start + "-" + end + " broj query-a: " + res.length)
        this.allPropertyes = res
        this.fiterProperty()
      })

    }
    else {
      this.fiterProperty();

    }
    
  }

  fiterProperty() {
    let filteredProperyes: PropertyDTO[] = [];
    // Initiator and event type filter
    this.propertyes = this.allPropertyes;
    filteredProperyes = this.propertyes.filter(event => event.propertyType.toLowerCase().includes(this.filterPropType.toLowerCase()) &&
      event.city.country.name.toLowerCase().includes(this.filterCountry.toLowerCase()) &&
      event.city.name.toLowerCase().includes(this.filterCity.toLowerCase()) &&
      event.propertyOwner.email.toLowerCase().includes(this.filterEmail.toLowerCase()) &&
      (event.propertyOwner.name + " " + event.propertyOwner.lastName).toLowerCase().includes(this.filterOwnerName.toLowerCase())

    );
    this.propertyes = filteredProperyes;
    this.dataSourceProperty = new MatTableDataSource<PropertyDTO>(this.propertyes);
    this.dataSourceProperty.sort = this.sort;
    this.dataSourceProperty.paginator = this.paginator;
    this.lengthProperty = this.propertyes.length;
  }

  clearFilterCity() {
    this.filterAppliedCity = false;
    this.filterCountry2 =''
    this.filterCity2 = ''
    this.cityes = this.allCityes
    this.dataSourceCity = new MatTableDataSource<CityOverview>(this.cityes);
    this.dataSourceCity.sort = this.sortCity;
    this.dataSourceCity.paginator = this.paginatorCity;
    this.lengthCity = this.cityes.length;
    }
    
    applyFilterCity() {
      this.filterAppliedCity= true;
      if ((this.rangeCity.value.start != null && this.rangeCity.value.start != null) 
      && this.rangeCity.controls.start.valid && this.rangeCity.controls.end.valid) { 
        let start = Math.floor(this.rangeCity.value.start!.getTime())/1000
        let end = Math.floor(this.rangeCity.value.end!.getTime())/1000
        this.propertyService.getByCityChart(start, end).subscribe((res: any) => {
          console.log(res);
          this.pyChart.options.data[0].dataPoints = res;
          this.pyChart.render();
        });
        this.propertyService.getCityForOverview(start, end).subscribe((res: any) => {
          this.cityes = res
          console.log('grad' + start + "-" + end + " broj query-a: " + res.length)
          this.allCityes = res
          this.FilterFieldsCity()
        })
  
      }
      else {
        this.FilterFieldsCity();
  
      }
    }


  private FilterFieldsCity() {
    let filteredCityes: CityOverview[] = [];
    // Initiator and event type filter
    this.cityes = this.allCityes;
    filteredCityes = this.cityes.filter(event => event.city.country.name.toLowerCase().includes(this.filterCountry2.toLowerCase()) &&
      event.city.name.toLowerCase().includes(this.filterCity2.toLowerCase())
    );
    this.cityes = filteredCityes;
    this.dataSourceCity = new MatTableDataSource<CityOverview>(this.cityes);
    this.dataSourceCity.sort = this.sortCity;
    this.dataSourceCity.paginator = this.paginatorCity;
    this.lengthCity = this.cityes.length;
  }

    goToSingleCity(city: CityOverview) {
      this.propertyService.setCity(city);
      this.router.navigate(['/city-overview']);
      }

    goToSingleProperty(property: PropertyDTO) {
      this.propertyService.setProperty(property);
      this.router.navigate(['/property-consumption-overview']);
      }

}

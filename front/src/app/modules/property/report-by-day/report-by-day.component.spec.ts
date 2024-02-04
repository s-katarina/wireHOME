import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ReportByDayComponent } from './report-by-day.component';

describe('ReportByDayComponent', () => {
  let component: ReportByDayComponent;
  let fixture: ComponentFixture<ReportByDayComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ReportByDayComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ReportByDayComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

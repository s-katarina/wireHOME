import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ReportByWeekComponent } from './report-by-week.component';

describe('ReportByWeekComponent', () => {
  let component: ReportByWeekComponent;
  let fixture: ComponentFixture<ReportByWeekComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ReportByWeekComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ReportByWeekComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ReportByDeviceTypeComponent } from './report-by-device-type.component';

describe('ReportByDeviceTypeComponent', () => {
  let component: ReportByDeviceTypeComponent;
  let fixture: ComponentFixture<ReportByDeviceTypeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ReportByDeviceTypeComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ReportByDeviceTypeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

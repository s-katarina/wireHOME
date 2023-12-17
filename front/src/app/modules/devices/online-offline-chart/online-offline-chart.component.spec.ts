import { ComponentFixture, TestBed } from '@angular/core/testing';

import { OnlineOfflineChartComponent } from './online-offline-chart.component';

describe('OnlineOfflineChartComponent', () => {
  let component: OnlineOfflineChartComponent;
  let fixture: ComponentFixture<OnlineOfflineChartComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ OnlineOfflineChartComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(OnlineOfflineChartComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

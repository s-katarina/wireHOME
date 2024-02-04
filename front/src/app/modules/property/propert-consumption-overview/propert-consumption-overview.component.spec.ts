import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PropertConsumptionOverviewComponent } from './propert-consumption-overview.component';

describe('PropertConsumptionOverviewComponent', () => {
  let component: PropertConsumptionOverviewComponent;
  let fixture: ComponentFixture<PropertConsumptionOverviewComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ PropertConsumptionOverviewComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PropertConsumptionOverviewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

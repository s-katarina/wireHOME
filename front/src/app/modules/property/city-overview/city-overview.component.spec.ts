import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CityOverviewComponent } from './city-overview.component';

describe('CityOverviewComponent', () => {
  let component: CityOverviewComponent;
  let fixture: ComponentFixture<CityOverviewComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CityOverviewComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CityOverviewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

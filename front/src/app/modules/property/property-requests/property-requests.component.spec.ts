import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PropertyRequestsComponent } from './property-requests.component';

describe('PropertyRequestsComponent', () => {
  let component: PropertyRequestsComponent;
  let fixture: ComponentFixture<PropertyRequestsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ PropertyRequestsComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PropertyRequestsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

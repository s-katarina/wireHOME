import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PropertyImgComponent } from './property-img.component';

describe('PropertyImgComponent', () => {
  let component: PropertyImgComponent;
  let fixture: ComponentFixture<PropertyImgComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ PropertyImgComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PropertyImgComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

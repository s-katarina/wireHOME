import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AdminPropertyOverviewComponent } from './admin-property-overview.component';

describe('AdminPropertyOverviewComponent', () => {
  let component: AdminPropertyOverviewComponent;
  let fixture: ComponentFixture<AdminPropertyOverviewComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AdminPropertyOverviewComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AdminPropertyOverviewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

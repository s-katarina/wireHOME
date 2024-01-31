import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SprinklerComponent } from './sprinkler.component';

describe('SprinklerComponent', () => {
  let component: SprinklerComponent;
  let fixture: ComponentFixture<SprinklerComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ SprinklerComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SprinklerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

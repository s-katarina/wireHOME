import { TestBed } from '@angular/core/testing';

import { LargeEnergyService } from './large-energy.service';

describe('LargeEnergyService', () => {
  let service: LargeEnergyService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(LargeEnergyService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});

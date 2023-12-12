import { TestBed } from '@angular/core/testing';

import { LampService } from './lamp.service';

describe('LampService', () => {
  let service: LampService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(LampService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});

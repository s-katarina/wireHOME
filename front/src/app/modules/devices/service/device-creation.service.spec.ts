import { TestBed } from '@angular/core/testing';

import { DeviceCreationService } from './device-creation.service';

describe('DeviceCreationService', () => {
  let service: DeviceCreationService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(DeviceCreationService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});

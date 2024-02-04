import { TestBed } from '@angular/core/testing';

import { IndoorDeviceService } from './indoor-device.service';

describe('IndoorDeviceService', () => {
  let service: IndoorDeviceService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(IndoorDeviceService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});

package projectnwt2023.backend.devices.service.interfaces;

import projectnwt2023.backend.devices.Device;

public interface IDeviceService {
    public Device save(Device device);
    public Device getById(Long id);

}

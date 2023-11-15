package projectnwt2023.backend.devices.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import projectnwt2023.backend.devices.Device;
import projectnwt2023.backend.devices.repository.DeviceRepository;
import projectnwt2023.backend.devices.service.interfaces.IRegistrationService;

@Service
public class RegistrationService implements IRegistrationService {

    @Autowired
    DeviceRepository deviceRepository;

    @Override
    public Device save(Device device) {
        return deviceRepository.save(device);
    }
}

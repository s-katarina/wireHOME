package projectnwt2023.backend.devices.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import projectnwt2023.backend.devices.repository.DeviceRepository;
import projectnwt2023.backend.devices.service.interfaces.IAmbientSensorService;

@Service
public class AmbientSensorService implements IAmbientSensorService {

    @Autowired
    private DeviceRepository deviceRepository;

}

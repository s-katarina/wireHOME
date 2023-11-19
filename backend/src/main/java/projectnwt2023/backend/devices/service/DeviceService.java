package projectnwt2023.backend.devices.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import projectnwt2023.backend.devices.Device;
import projectnwt2023.backend.devices.repository.DeviceRepository;
import projectnwt2023.backend.devices.service.interfaces.IDeviceService;
import projectnwt2023.backend.exceptions.EntityNotFoundException;

import java.util.Optional;

@Service
public class DeviceService implements IDeviceService {

    @Autowired
    DeviceRepository deviceRepository;

    @Override
    public Device save(Device device) {
        Device saved = deviceRepository.save(device);
        saved.setImagePath((String.format("device-%s.jpg", saved.getId())));
        return deviceRepository.save(saved);
    }

    @Override
    public Device getById(Long id) {
        Optional<Device> device = deviceRepository.findById(id);

        if (!device.isPresent())
            throw new EntityNotFoundException(Device.class);

        return device.get();
    }
}

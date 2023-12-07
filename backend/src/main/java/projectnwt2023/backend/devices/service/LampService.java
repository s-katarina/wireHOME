package projectnwt2023.backend.devices.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import projectnwt2023.backend.devices.Device;
import projectnwt2023.backend.devices.Lamp;
import projectnwt2023.backend.devices.State;
import projectnwt2023.backend.devices.mqtt.Gateway;
import projectnwt2023.backend.devices.repository.DeviceRepository;
import projectnwt2023.backend.devices.service.interfaces.ILampService;
import projectnwt2023.backend.exceptions.EntityNotFoundException;
import projectnwt2023.backend.property.Property;

import javax.swing.text.html.Option;
import java.util.Optional;

@Service
public class LampService implements ILampService {

    @Autowired
    DeviceRepository deviceRepository;

    @Override
    public Lamp changeBulbState(Long lampId, String usedFor) {
        Optional<Device> device = deviceRepository.findById(lampId);
        if (!device.isPresent()) {
            throw new EntityNotFoundException(Lamp.class);
        }

        boolean state;
        if (usedFor.equals("OFF")) state = false;
        else if (usedFor.equals("ON")) state = true;
        else return null;

        Lamp lamp = (Lamp) device.get();
        lamp.setBulbState(state);
        System.out.println("Changed bulb state to " + state);
        return deviceRepository.save(lamp);
    }


}

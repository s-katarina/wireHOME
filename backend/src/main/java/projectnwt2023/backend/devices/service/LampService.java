package projectnwt2023.backend.devices.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import projectnwt2023.backend.devices.Device;
import projectnwt2023.backend.devices.Lamp;
import projectnwt2023.backend.devices.State;
import projectnwt2023.backend.devices.dto.PayloadDTO;
import projectnwt2023.backend.devices.mqtt.Gateway;
import projectnwt2023.backend.devices.repository.DeviceRepository;
import projectnwt2023.backend.devices.service.interfaces.ILampService;
import projectnwt2023.backend.exceptions.EntityNotFoundException;
import projectnwt2023.backend.property.Property;

import javax.swing.text.html.Option;
import java.util.Optional;

import static projectnwt2023.backend.helper.RegexPattern.isStringMatchingPattern;

@Service
public class LampService implements ILampService {

    @Autowired
    DeviceRepository deviceRepository;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Override
    public void parseRequest(String topic, PayloadDTO payloadDTO) {
        if (isStringMatchingPattern(topic, "lamp/\\d+/bulb")) {
            changeBulbState((long) payloadDTO.getDeviceId(), payloadDTO.getUsedFor());
        } else if (isStringMatchingPattern(topic, "lamp/\\d+/automatic")) {
            setAutomaticRegime((long) payloadDTO.getDeviceId(), payloadDTO.getUsedFor());
        }
    }

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
        this.simpMessagingTemplate.convertAndSend("/lamp/" + lampId, "Bulb state changed to " + state);
        return deviceRepository.save(lamp);
    }

    @Override
    public Lamp setAutomaticRegime(Long lampId, String usedFor) {
        Optional<Device> device = deviceRepository.findById(lampId);
        if (!device.isPresent()) {
            throw new EntityNotFoundException(Lamp.class);
        }

        Lamp lamp = (Lamp) device.get();
        boolean automatic = usedFor.equals("ON");
        lamp.setAutomatic(automatic);
        System.out.println("Changed automatic to " + automatic);
        return deviceRepository.save(lamp);
    }


}

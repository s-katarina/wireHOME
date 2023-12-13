package projectnwt2023.backend.devices.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import projectnwt2023.backend.devices.Device;
import projectnwt2023.backend.devices.Gate;
import projectnwt2023.backend.devices.Lamp;
import projectnwt2023.backend.devices.dto.GateDTO;
import projectnwt2023.backend.devices.dto.LampDTO;
import projectnwt2023.backend.devices.dto.PayloadDTO;
import projectnwt2023.backend.devices.repository.DeviceRepository;
import projectnwt2023.backend.devices.service.interfaces.IGateService;
import projectnwt2023.backend.exceptions.EntityNotFoundException;

import java.util.Optional;

import static projectnwt2023.backend.helper.RegexPattern.isStringMatchingPattern;

@Service
public class GateService implements IGateService {

    @Autowired
    DeviceRepository deviceRepository;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Override
    public void parseRequest(String topic, PayloadDTO payloadDTO) {
        if (isStringMatchingPattern(topic, "gate/\\d+/regime")) {
            changeGateRegime((long) payloadDTO.getDeviceId(), payloadDTO.getUsedFor());
        } else if (isStringMatchingPattern(topic, "gate/\\d+/open")) {
            changeGateOpen((long) payloadDTO.getDeviceId(), payloadDTO.getUsedFor());
        }
    }

    @Override
    public Gate changeGateRegime(Long gateId, String regime) {
        Optional<Device> device = deviceRepository.findById(gateId);
        if (!device.isPresent()) {
            throw new EntityNotFoundException(Lamp.class);
        }

        boolean isPublic;
        if (regime.equals("PUBLIC")) isPublic = true;
        else if (regime.equals("PRIVATE")) isPublic = false;
        else return null;

        Gate gate = (Gate) device.get();
        gate.setPublic(isPublic);
        System.out.println("Changed gate regime (public) to " + isPublic);
        this.simpMessagingTemplate.convertAndSend("/gate/" + gateId, new GateDTO(gate));
        return deviceRepository.save(gate);
    }

    @Override
    public Gate changeGateOpen(Long gateId, String openClose) {
        Optional<Device> device = deviceRepository.findById(gateId);
        if (!device.isPresent()) {
            throw new EntityNotFoundException(Lamp.class);
        }

        boolean open;
        if (openClose.equals("OPEN")) open = true;
        else if (openClose.equals("CLOSE")) open = false;
        else return null;

        Gate gate = (Gate) device.get();
        gate.setOpen(open);
        System.out.println("Changed gate open to " + open);
        this.simpMessagingTemplate.convertAndSend("/gate/" + gateId, new GateDTO(gate));
        return deviceRepository.save(gate);
    }
}

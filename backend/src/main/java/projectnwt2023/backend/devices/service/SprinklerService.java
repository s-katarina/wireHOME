package projectnwt2023.backend.devices.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import projectnwt2023.backend.devices.Device;
import projectnwt2023.backend.devices.Lamp;
import projectnwt2023.backend.devices.Sprinkler;
import projectnwt2023.backend.devices.dto.PayloadWithCallerDTO;
import projectnwt2023.backend.devices.dto.PayloadDTO;
import projectnwt2023.backend.devices.dto.SprinklerScheduleDTO;
import projectnwt2023.backend.devices.dto.model.SprinklerDTO;
import projectnwt2023.backend.devices.mqtt.Beans;
import projectnwt2023.backend.devices.repository.DeviceRepository;
import projectnwt2023.backend.devices.service.interfaces.ISprinklerService;
import projectnwt2023.backend.exceptions.EntityNotFoundException;

import java.util.Arrays;
import java.util.Optional;

import static projectnwt2023.backend.helper.RegexPattern.isStringMatchingPattern;

@Service
public class SprinklerService implements ISprinklerService {
    @Autowired
    DeviceRepository deviceRepository;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Override
    public void parseRequest(String topic, Message<?> request) {
        System.out.println(topic);
        if (isStringMatchingPattern(topic, "sprinkler/\\d+/on")) {
            PayloadDTO payloadDTO = Beans.getPayload(request, PayloadWithCallerDTO.class);
            changeOn((long) payloadDTO.getDeviceId(), payloadDTO.getUsedFor());
        } else if (isStringMatchingPattern(topic, "sprinkler/\\d+/schedule")) {
            SprinklerScheduleDTO payloadDTO = Beans.getPayload(request, SprinklerScheduleDTO.class);
            setSchedule(payloadDTO);
        } else if (isStringMatchingPattern(topic, "sprinkler/\\d+/schedule/off")) {
            PayloadDTO payloadDTO = Beans.getPayload(request, PayloadWithCallerDTO.class);
            turnOffSchedule(payloadDTO.getDeviceId());
        }
    }

    @Override
    public Sprinkler changeOn(Long sprinklerId, String usedFor) {
        Optional<Device> device = deviceRepository.findById(sprinklerId);
        if (!device.isPresent()) {
            throw new EntityNotFoundException(Lamp.class);
        }

        boolean newOn;
        if (usedFor.equals("OFF")) newOn = false;
        else if (usedFor.equals("ON")) newOn = true;
        else return null;

        Sprinkler sprinkler = (Sprinkler) device.get();
        sprinkler.setOn(newOn);
        System.out.println("Changed sprinkler on/off to " + newOn);
        // Update web sockets for frontend
        this.simpMessagingTemplate.convertAndSend("/sprinkler/" + sprinklerId, new SprinklerDTO(sprinkler));
        return deviceRepository.save(sprinkler);
    }

    @Override
    public Sprinkler setSchedule(SprinklerScheduleDTO sprinklerScheduleDTO) {
        Optional<Device> device = deviceRepository.findById(sprinklerScheduleDTO.getDeviceId().longValue());
        if (!device.isPresent()) {
            throw new EntityNotFoundException(Lamp.class);
        }

        System.out.println(Arrays.toString(sprinklerScheduleDTO.getWeekdays()));
        Sprinkler sprinkler = (Sprinkler) device.get();
        sprinkler.setScheduleMode(true);
        sprinkler.CustomSetSchedule(sprinklerScheduleDTO.getStartHour(),
                sprinklerScheduleDTO.getEndHour(),
                sprinklerScheduleDTO.getWeekdays());
        System.out.println("Changed sprinkler schedule " );
        Sprinkler s = deviceRepository.save(sprinkler);
        System.out.println(sprinkler.getSchedule().getWeekdays());
        // Update web sockets for frontend
        this.simpMessagingTemplate.convertAndSend("/sprinkler/" + sprinkler.getId(), new SprinklerDTO(sprinkler));
        return s;
    }

    @Override
    public Sprinkler turnOffSchedule(Integer deviceId) {
        Optional<Device> device = deviceRepository.findById(deviceId.longValue());
        if (!device.isPresent()) {
            throw new EntityNotFoundException(Lamp.class);
        }

        Sprinkler sprinkler = (Sprinkler) device.get();
        sprinkler.setScheduleMode(false);

        Sprinkler s = deviceRepository.save(sprinkler);
        this.simpMessagingTemplate.convertAndSend("/sprinkler/" + sprinkler.getId(), new SprinklerDTO(sprinkler));
        System.out.println("Changed sprinkler schedule mode to " + sprinkler.isScheduleMode() );
        return s;
    }

}

package projectnwt2023.backend.devices.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import projectnwt2023.backend.devices.Device;
import projectnwt2023.backend.devices.Gate;
import projectnwt2023.backend.devices.Lamp;
import projectnwt2023.backend.devices.dto.*;
import projectnwt2023.backend.devices.mqtt.Beans;
import projectnwt2023.backend.devices.repository.DeviceRepository;
import projectnwt2023.backend.devices.service.interfaces.IGateService;
import projectnwt2023.backend.exceptions.EntityNotFoundException;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static projectnwt2023.backend.helper.RegexPattern.isStringMatchingPattern;

@Service
public class GateService implements IGateService {

    @Autowired
    DeviceRepository deviceRepository;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    InfluxDBService influxDBService;

    @Override
    public void parseRequest(String topic, Message<?> message) {
        System.out.println(topic);
        if (isStringMatchingPattern(topic, "gate/\\d+/regime")) {
            PayloadDTO payloadDTO = Beans.getPayload(message, GateEventPayloadDTO.class);
            changeGateRegime((long) payloadDTO.getDeviceId(), payloadDTO.getUsedFor());
        } else if (isStringMatchingPattern(topic, "gate/\\d+/open")) {
            PayloadDTO payloadDTO = Beans.getPayload(message, GateEventPayloadDTO.class);
            changeGateOpen((long) payloadDTO.getDeviceId(), payloadDTO.getUsedFor());
        } else if (isStringMatchingPattern(topic, "gate/\\d+/event")) {
            processEvent((String) message.getPayload());
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

    // For real-time updates
    @Override
    public void processEvent(String payload) {
        String pattern = "gate-event,device-id=(\\d+) value=\"([^\"]*)\",caller=\"([^\"]*)\"";

        Pattern r = Pattern.compile(pattern);
        Matcher matcher = r.matcher(payload);
        if (matcher.find()) {
            String deviceId = matcher.group(1);
            String value = matcher.group(2);
            String caller = matcher.group(3);

            this.simpMessagingTemplate.convertAndSend("/gate/" + deviceId + "/event", new GateEventDTO(caller, value, String.valueOf(System.currentTimeMillis())));

        } else {
            System.out.println("No match found");
        }
    }

    @Override
    public List<GateEventMeasurement> getRecentGateEvents(Long gateId) {
        Optional<Device> device = deviceRepository.findById(gateId);
        if (!device.isPresent()) {
            throw new EntityNotFoundException(Lamp.class);
        }

        return influxDBService.findRecentGateEvents(gateId.toString());
    }

    @Override
    public List<GateEventMeasurement> getDateRangeGateEvents(Long gateId, String start, String end) {
        Optional<Device> device = deviceRepository.findById(gateId);
        if (!device.isPresent()) {
            throw new EntityNotFoundException(Lamp.class);
        }

        try {
            List<GateEventMeasurement> res = influxDBService.findDateRangeGateEvents(String.valueOf(gateId), Long.parseLong(start), Long.parseLong(end));
            return res;
        } catch (NumberFormatException e) {
            return null;
        }

    }

    @Override
    public Gate addLicencePlate(Long gateId, String licencePlate) {
        Optional<Device> device = deviceRepository.findById(gateId);
        if (!device.isPresent()) {
            throw new EntityNotFoundException(Lamp.class);
        }
        Gate gate = (Gate) device.get();
        if (!gate.getLicencePlates().contains(licencePlate)) {
            gate.addLicencePlate(licencePlate);
        }
        return deviceRepository.save(gate);
    }
}

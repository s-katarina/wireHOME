package projectnwt2023.backend.devices.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;
import projectnwt2023.backend.devices.dto.GateEventPayloadDTO;
import projectnwt2023.backend.devices.dto.PayloadDTO;
import projectnwt2023.backend.devices.mqtt.Beans;
import projectnwt2023.backend.devices.repository.DeviceRepository;
import projectnwt2023.backend.devices.service.interfaces.IAmbientSensorService;

import static projectnwt2023.backend.helper.RegexPattern.isStringMatchingPattern;

@Service
public class AmbientSensorService implements IAmbientSensorService {

    @Autowired
    private DeviceRepository deviceRepository;

    @Override
    public void parseRequest(String topic, Message<?> message) {
        if (isStringMatchingPattern(topic, "ambientSensor/\\d+/temp")) {
            Double temp = 0.0;
            String payload = (String) message.getPayload();
            temp = Double.valueOf(payload.split("value")[1].substring(1));
        }
        if (isStringMatchingPattern(topic, "ambientSensor/\\d+/hum")) {
            Double hum = 0.0;
            String payload = (String) message.getPayload();
            hum = Double.valueOf(payload.split("value")[1].substring(1));
        }
    }
}

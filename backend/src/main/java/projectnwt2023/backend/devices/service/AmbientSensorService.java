package projectnwt2023.backend.devices.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import projectnwt2023.backend.devices.repository.DeviceRepository;
import projectnwt2023.backend.devices.service.interfaces.IAmbientSensorService;

import static projectnwt2023.backend.helper.RegexPattern.isStringMatchingPattern;

@Service
public class AmbientSensorService implements IAmbientSensorService {

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Override
    public void parseRequest(String topic, Message<?> message) {
        if (isStringMatchingPattern(topic, "ambientSensor/\\d+/temp")) {
            String payload = (String) message.getPayload();
            Double temp = Double.valueOf(payload.split("value")[1].substring(1));
            Integer deviceId = Integer.valueOf(payload.split("=")[1].split(" ")[0]);
            System.out.println("asd" + deviceId);
            simpMessagingTemplate.convertAndSend("/ambient-sensor/" + deviceId + "/temp", temp);
        }
        if (isStringMatchingPattern(topic, "ambientSensor/\\d+/hum")) {
            String payload = (String) message.getPayload();
            Double hum = Double.valueOf(payload.split("value")[1].substring(1));
            Integer deviceId = Integer.valueOf(payload.split("=")[1].split(" ")[0]);
            simpMessagingTemplate.convertAndSend("/ambient-sensor/" + deviceId + "/hum", hum);
        }
    }
}

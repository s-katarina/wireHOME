package projectnwt2023.backend.devices.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import projectnwt2023.backend.devices.repository.DeviceRepository;
import projectnwt2023.backend.devices.service.interfaces.IAirConditionerService;

import static projectnwt2023.backend.helper.RegexPattern.isStringMatchingPattern;

@Service
public class AirConditionerService implements IAirConditionerService {

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Override
    public void parseRequest(String topic, Message<?> message) {
        if (isStringMatchingPattern(topic, "airConditioner/\\d+/response")) {
            String payload = (String) message.getPayload();
            System.out.println("payload: " + payload);
            String response = payload.split(";")[0];
            Integer deviceId = Integer.valueOf(payload.split(";")[1]);
            System.out.println("/air-conditioner/" + deviceId + "/response");
            simpMessagingTemplate.convertAndSend("/air-conditioner/" + deviceId + "/response", response);
        }
    }

}

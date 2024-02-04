package projectnwt2023.backend.devices.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import projectnwt2023.backend.devices.AirConditioner;
import projectnwt2023.backend.devices.Charger;
import projectnwt2023.backend.devices.dto.CarDTO;
import projectnwt2023.backend.devices.repository.DeviceRepository;
import projectnwt2023.backend.devices.service.interfaces.IChargerService;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static projectnwt2023.backend.helper.RegexPattern.isStringMatchingPattern;

@Service
public class ChargerService implements IChargerService {
    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Override
    public void parseRequest(String topic, Message<?> request) {
        if (isStringMatchingPattern(topic, "charger/\\d+/event")) {
            changePortNumber(request);
        }
        if (isStringMatchingPattern(topic, "charger/\\d+/car")) {
            parseCarInfo(request);
        }
    }

    private void parseCarInfo(Message<?> request) {
        String payload = (String) request.getPayload();
        String pattern = "device-id=(\\d+),battery-capacity=(\\d+),percentage=(\\d+),energy-consumed=([\\d.]+) value=\"([^\"]+)\"";
        Pattern regexPattern = Pattern.compile(pattern);

        // Create a matcher for the input string
        Matcher matcher = regexPattern.matcher(payload);

        // Check if the pattern matches
        if (matcher.find()) {
            // Extract values using group indexes
            int deviceId = Integer.parseInt(matcher.group(1));
            int batteryCapacity = Integer.parseInt(matcher.group(2));
            int percentage = Integer.parseInt(matcher.group(3));
            double energyConsumed = Double.parseDouble(matcher.group(4));
            String value = matcher.group(5);

            simpMessagingTemplate.convertAndSend("/charger/" + deviceId + "/car", new CarDTO(value, batteryCapacity, percentage, energyConsumed));

        } else {
            System.out.println("Pattern not found in the input string.");
        }
    }

    private void changePortNumber(Message<?> request) {
        String payload = (String) request.getPayload();
        String reason = payload.split(" ")[1].split(",")[0];
        String device = payload.split(" ")[0].split(",")[1].replaceAll("device-id=", "");
        Integer deviceId = Integer.valueOf(device);
        Charger charger = (Charger) deviceRepository.findById(deviceId.longValue()).get();
        int newAvailableNum = charger.getAvailablePortNumber() - 1;
        if (reason.contains("stop")) {
            newAvailableNum = charger.getAvailablePortNumber() + 1;
        }
        if (newAvailableNum >= 0 && newAvailableNum<=charger.getPortNumber()){
            charger.setAvailablePortNumber(newAvailableNum);
            deviceRepository.save(charger);
            simpMessagingTemplate.convertAndSend("/charger/" + deviceId, newAvailableNum);
        }

    }
}

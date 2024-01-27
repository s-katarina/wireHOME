package projectnwt2023.backend.devices.service.interfaces;

import org.springframework.messaging.Message;
import projectnwt2023.backend.devices.Sprinkler;
import projectnwt2023.backend.devices.dto.SprinklerScheduleDTO;

public interface ISprinklerService {
    public void parseRequest(String topic, Message<?> request);
    public Sprinkler changeOn(Long sprinklerId, String usedFor);

    Sprinkler setSchedule(SprinklerScheduleDTO sprinklerScheduleDTO);
}
